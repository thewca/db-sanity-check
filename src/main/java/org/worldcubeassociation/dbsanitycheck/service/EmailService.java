package org.worldcubeassociation.dbsanitycheck.service;

import org.worldcubeassociation.dbsanitycheck.bean.AnalysisBean;
import org.worldcubeassociation.dbsanitycheck.bean.SanityCheckWithErrorBean;

import java.util.List;
import javax.mail.MessagingException;

@FunctionalInterface
public interface EmailService {
    void sendEmail(String emailTo, List<AnalysisBean> analysisResult, List<SanityCheckWithErrorBean> queriesWithError)
            throws MessagingException;
}
