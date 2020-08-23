package org.worldcubeassociation.dbsanitycheck.service;

import java.io.FileNotFoundException;

import javax.mail.MessagingException;

import org.worldcubeassociation.dbsanitycheck.exception.SanityCheckException;

@FunctionalInterface
public interface WrtSanityCheckService {
	public void execute() throws FileNotFoundException, SanityCheckException, MessagingException;
}
