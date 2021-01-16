package org.worldcubeassociation.dbsanitycheck.service;

import java.util.List;

import javax.mail.MessagingException;

import org.worldcubeassociation.dbsanitycheck.bean.AnalysisBean;
import org.worldcubeassociation.dbsanitycheck.bean.QueryWithErrorBean;

@FunctionalInterface
public interface EmailService {
	public void sendEmail(List<AnalysisBean> analysisResult, List<QueryWithErrorBean> queriesWithError)
			throws MessagingException;
}
