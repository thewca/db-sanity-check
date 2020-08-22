package org.worldcubeassociation.dbsanitycheck.service;

import java.util.List;

import javax.mail.MessagingException;

import org.worldcubeassociation.dbsanitycheck.bean.AnalysisBean;

@FunctionalInterface
public interface EmailService {
	public void sendEmail(List<AnalysisBean> analysisResult) throws MessagingException;
}
