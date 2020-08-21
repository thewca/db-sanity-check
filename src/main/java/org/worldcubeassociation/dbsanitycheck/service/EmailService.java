package org.worldcubeassociation.dbsanitycheck.service;

@FunctionalInterface
public interface EmailService {
	public void sendEmail(String content);

}
