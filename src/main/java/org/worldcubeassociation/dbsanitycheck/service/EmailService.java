package org.worldcubeassociation.dbsanitycheck.service;

import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

@FunctionalInterface
public interface EmailService {
	public void sendEmail(Map<String, List<String>> analysis) throws MessagingException;
}
