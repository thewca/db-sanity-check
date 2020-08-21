package org.worldcubeassociation.dbsanitycheck.service.impl;

import java.io.File;
import java.util.List;
import java.util.Map;

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

			MimeMessageHelper helper = new MimeMessageHelper(message, true);

			helper.setFrom("acampos@worldcubeassociation.org");
			helper.setTo(emailTo);
			helper.setSubject(subject);
			helper.setText("Done, test");

			// Email the log file
			FileSystemResource file = new FileSystemResource(new File("log/db-sanity-check.log"));
			helper.addAttachment("db-sanity-check.log", file);

			emailSender.send(message);
		} else {
			log.info("Not sending email");
		}

	}

}
