package org.worldcubeassociation.dbsanitycheck.service.impl;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.worldcubeassociation.dbsanitycheck.service.EmailService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

	@Value("${service.mail.send}")
	private boolean sendMail;

	@Value("${service.mail.to}")
	private String emailTo;

	@Value("${service.mail.subject}")
	private String subject;

	@Autowired
	private JavaMailSender emailSender;

	@Override
	public void sendEmail(Map<String, List<String>> analysis) throws MessagingException {
		if (sendMail) {
			log.info("Sending email with the analysis");

			MimeMessage message = emailSender.createMimeMessage();

			boolean multipart = true;
			MimeMessageHelper helper = new MimeMessageHelper(message, multipart);

			helper.setFrom("acampos@worldcubeassociation.org");
			helper.setTo(emailTo);
			helper.setSubject(subject);

			boolean html = true;
			helper.setText(getText(analysis), html);

			// Email the log file
			FileSystemResource file = new FileSystemResource(new File("log/db-sanity-check.log"));
			helper.addAttachment("db-sanity-check.log", file);

			emailSender.send(message);
		} else {
			log.info("Not sending email");
		}

	}

	private String getText(Map<String, List<String>> analysis) {
		StringBuilder sb = new StringBuilder("<h3>Sanity Check Results</h3>\n\n");

		if (analysis.size() == 0) {
			sb.append("<p>No results to show</p>\n");
		} else {
			sb.append("<p>Found inconsistencies in ").append(analysis.size()).append(" topics.</p>\n\n");
		}

		for (Entry<String, List<String>> item : analysis.entrySet()) {
			sb.append("<table style=\"border: 1px solid black;\">\n");
			sb.append(" <thead>\n");
			sb.append("  <tr><th scope=\"col\" style=\"background-color: #f2f2f2;border: 1px solid black;\">")
					.append(item.getKey()).append("</th></tr>\n");
			sb.append(" </thead>\n");
			sb.append(" <tbody >\n");
			for (String line : item.getValue()) {
				sb.append("  <tr>\n");
				sb.append("   <td style=\"border: 1px solid black;\">\n").append(line).append("</td>\n");
				sb.append("  </tr>\n");
			}
			sb.append(" </tbody>\n");
			sb.append("</table>\n");
			sb.append("<br>\n\n");
		}

		return sb.toString();
	}

}
