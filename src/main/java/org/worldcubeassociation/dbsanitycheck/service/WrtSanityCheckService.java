package org.worldcubeassociation.dbsanitycheck.service;

import javax.mail.MessagingException;

@FunctionalInterface
public interface WrtSanityCheckService {
	void execute() throws MessagingException;
}
