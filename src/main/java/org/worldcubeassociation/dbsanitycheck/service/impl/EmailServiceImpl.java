package org.worldcubeassociation.dbsanitycheck.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.worldcubeassociation.dbsanitycheck.bean.AnalysisBean;
import org.worldcubeassociation.dbsanitycheck.bean.SanityCheckWithErrorBean;
import org.worldcubeassociation.dbsanitycheck.model.SanityCheck;
import org.worldcubeassociation.dbsanitycheck.service.EmailService;
import org.worldcubeassociation.dbsanitycheck.service.ExclusionService;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Value("${service.mail.send}")
    private boolean sendMail;

    @Value("${service.mail.to}")
    private String mailTo;

    @Value("${service.mail.from}")
    private String mailFrom;

    @Value("${service.mail.subject}")
    private String subject;

    @Value("${service.mail.logfilepath}")
    private String logFilePath;

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private ExclusionService exclusionService;

    private static final boolean MULTIPART = true;
    private static final int EMAIL_TRUNCATION_LIMIT = 50;

    @Override
    public void sendEmail(List<AnalysisBean> analysisResult, List<SanityCheckWithErrorBean> queriesWithError)
            throws MessagingException {
        if (sendMail) {
            log.info("Sending email with the analysis");

            MimeMessage message = emailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, MULTIPART);

            helper.setFrom(mailFrom);
            handleRecipients(helper);
            LocalDate currentDate = LocalDate.now();
            String formattedSubject = subject + " - " + currentDate.getMonth() + " " + currentDate.getYear();
            helper.setSubject(formattedSubject);

            log.info("Mail from: " + mailFrom);
            log.info("Mail to: " + mailTo);
            log.info("Subject: " + formattedSubject);

            boolean html = true;
            helper.setText(getText(analysisResult, queriesWithError), html);

            log.info("Attach log file");
            FileSystemResource file = new FileSystemResource(new File(logFilePath));
            helper.addAttachment("db-sanity-check.txt", file);

            ByteArrayResource exclusionSuggestion =
                    exclusionService.buildExclusionSuggestionFile(analysisResult);
            if (exclusionSuggestion != null) {
                // txt for better reading in the email
                helper.addAttachment("exclusion-suggestions.txt", exclusionSuggestion);
            }

            emailSender.send(message);

            log.info("Email sent.");
        } else {
            log.info("Not sending email");
        }

    }

    private void handleRecipients(MimeMessageHelper helper) throws MessagingException {
        var mailSplit = List.of(mailTo.split(","));
        helper.setTo(InternetAddress.parse(mailTo));
        helper.setReplyTo(mailSplit.get(0));
        if (mailSplit.size() > 1) {
            helper.setCc(InternetAddress.parse(
                    String.join(",", mailSplit.subList(1, mailSplit.size()))));
        }
    }

    private String getText(List<AnalysisBean> analysisResult, List<SanityCheckWithErrorBean> queriesWithError) {
        log.info("Build email content");

        StringBuilder sb = new StringBuilder("<h2>Sanity Check Results</h2>\n\n");

        if (analysisResult.isEmpty() && queriesWithError.isEmpty()) {
            sb.append("<p>No results to show</p>\n");
        }

        addAnalysis(analysisResult, sb);
        addErrors(queriesWithError, sb);

        return sb.toString();
    }

    private void addAnalysis(List<AnalysisBean> analysisResult, StringBuilder sb) {
        if (!analysisResult.isEmpty()) {
            sb.append("<h4>Found inconsistencies in ").append(analysisResult.size()).append(" topics.</h4>\n\n");
        } else {
            sb.append("<h4>No inconsistencies found.</h4>\n\n");
            return;
        }

        for (int i = 0; i < analysisResult.size() && i < EMAIL_TRUNCATION_LIMIT; i++) {
            AnalysisBean analysis = analysisResult.get(i);
            sb.append(String.format("<h3>%s. [%s] %s</h3>%n", i + 1,
                    analysis.getSanityCheck().getCategory().getName(),
                    analysis.getSanityCheck().getTopic()));
            sb.append("<div style=\"overflow-x: auto;\">\n");
            sb.append(" <table style=\"border: 1px solid black;\">\n");
            sb.append("  <thead>\n");
            sb.append("   <tr style=\"background-color: #f2f2f2;\">");
            List<String> headers = analysis.getKeys();
            for (String header : headers) {
                sb.append("<th scope=\"col\" style=\"border: 1px solid black;\">").append(header).append("</th>");
            }
            sb.append("\n   </tr>\n");
            sb.append("  </thead>\n");
            sb.append("  <tbody>\n");
            for (int j = 0; j < analysis.getAnalysis().size() && j < EMAIL_TRUNCATION_LIMIT; j++) {
                var item = analysis.getAnalysis().get(j);
                sb.append("   <tr>\n");
                for (String header : headers) {
                    sb.append("    <td style=\"border: 1px solid black;\">")
                            .append(item.has(header) ? item.get(header) : "-")
                            .append("</td>\n");
                }
                sb.append("   </tr>\n");
            }
            if (analysis.getAnalysis().size() > EMAIL_TRUNCATION_LIMIT) {
                sb.append(String.format("   <tr><td colspan=\"%s\" style=\"border: 1px solid black;\">%s other results "
                                + "truncated</td></tr>", headers.size(),
                        analysis.getAnalysis().size() - EMAIL_TRUNCATION_LIMIT));
            }
            sb.append("  </tbody>\n");
            sb.append(" </table>\n");
            sb.append("</div>\n");
            sb.append("<br>\n\n");
        }
        if (analysisResult.size() > EMAIL_TRUNCATION_LIMIT) {
            sb.append(
                    String.format("<h4>%s other queries truncated</h4>", analysisResult.size() - EMAIL_TRUNCATION_LIMIT)
            );
        }
    }

    private void addErrors(List<SanityCheckWithErrorBean> queriesWithError, StringBuilder sb) {
        if (!queriesWithError.isEmpty()) {
            sb.append("<h4>Found errors in ").append(queriesWithError.size()).append(" queries.</h4>\n\n");
        } else {
            sb.append("<h4>There is no query with SQL errors.</h4>\n\n");
            return;
        }

        for (int i = 0; i < queriesWithError.size(); i++) {
            SanityCheckWithErrorBean queryWithErrorBean = queriesWithError.get(i);
            SanityCheck sanityCheck = queryWithErrorBean.getSanityCheck();
            sb.append(String.format("<p>%s. [%s] %s</p>%n", i + 1, sanityCheck.getCategory().getName(),
                    sanityCheck.getTopic()));
            sb.append(String.format("<code>%s</code>\n", sanityCheck.getQuery()));
            sb.append(String.format("<p><b>Reason:</b> %s</p>\n", queryWithErrorBean.getError()));
            sb.append("<br>\n\n");
        }
    }
}
