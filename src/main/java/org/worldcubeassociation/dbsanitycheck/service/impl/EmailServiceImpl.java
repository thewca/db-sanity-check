package org.worldcubeassociation.dbsanitycheck.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
	public void sendEmail(String content) {
		if (sendMail) {
			log.info("Sending email with the analysis");

			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom("noreply@worldcubeassociation.org");
			message.setTo(emailTo);
			message.setSubject(subject);
			message.setText(content);
			emailSender.send(message);
		} else {
			log.info("Not sending email");
		}

	}

}
