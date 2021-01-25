package org.worldcubeassociation.dbsanitycheck.service.impl;

import ch.qos.logback.classic.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.util.ReflectionTestUtils;
import org.worldcubeassociation.dbsanitycheck.bean.AnalysisBean;
import org.worldcubeassociation.dbsanitycheck.bean.SanityCheckWithErrorBean;
import org.worldcubeassociation.dbsanitycheck.model.SanityCheck;
import org.worldcubeassociation.dbsanitycheck.service.SanityCheckExclusionService;
import org.worldcubeassociation.dbsanitycheck.util.LogUtil;
import org.worldcubeassociation.dbsanitycheck.util.StubUtil;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

public class EmailServiceImplTest {

    @InjectMocks
    private EmailServiceImpl emailService;

    @Captor
    private ArgumentCaptor<MimeMessage> emailCaptor;

    @Mock
    private JavaMailSender emailSender;

    @Mock
    private SanityCheckExclusionService sanityCheckExclusionService;

    private static final Random random = new Random();
    private static final int MAX_TOPICS_FOUND = 10;
    private static final int MAX_INCONSISTENCIES_FOUND = 5;
    private static final int MAX_COLUMNS_FOUND = 3;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(emailService, "mailFrom", "noreply@email.com");
        ReflectionTestUtils.setField(emailService, "mailTo", "1@email.com");
        ReflectionTestUtils.setField(emailService, "subject", "Sanity Check");
        ReflectionTestUtils.setField(emailService, "logFilePath", "log/db-sanity-check.log");
    }

    @Test
    public void sendEmailTest() throws MessagingException, JSONException {
        ReflectionTestUtils.setField(emailService, "sendMail", true);

        Logger log = LogUtil.getDefaultLogger(EmailServiceImpl.class);

        String textFile = "-- exclusion suggestion";

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        when(emailSender.createMimeMessage()).thenReturn(sender.createMimeMessage());
        when(sanityCheckExclusionService.buildExclusionSuggestionFile(anyList()))
                .thenReturn(new ByteArrayResource(textFile.getBytes(
                        StandardCharsets.UTF_8)));

        List<AnalysisBean> analysisResult = getDefaultAnalysis();
        List<SanityCheckWithErrorBean> queriesWithError = getDefaultQueriesWithError();

        emailService.sendEmail(analysisResult, queriesWithError);

        int logs = LogUtil.countLogsContaining(log, "Email sent.");
        assertEquals(1, logs);
    }

    @Test
    public void sendEmailFalseTest() throws MessagingException, JSONException {
        ReflectionTestUtils.setField(emailService, "sendMail", false);

        Logger log = LogUtil.getDefaultLogger(EmailServiceImpl.class);

        List<AnalysisBean> analysisResult = getDefaultAnalysis();
        List<SanityCheckWithErrorBean> queriesWithError = new ArrayList<>();

        emailService.sendEmail(analysisResult, queriesWithError);

        int logs = LogUtil.countLogsContaining(log, "Not sending email");
        assertEquals(1, logs);
    }

    @Test
    public void sendEmailNoResultsTest() throws MessagingException {
        ReflectionTestUtils.setField(emailService, "sendMail", true);

        Logger log = LogUtil.getDefaultLogger(EmailServiceImpl.class);

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        when(emailSender.createMimeMessage()).thenReturn(sender.createMimeMessage());

        List<AnalysisBean> analysisResult = new ArrayList<>();
        List<SanityCheckWithErrorBean> queriesWithError = new ArrayList<>();

        emailService.sendEmail(analysisResult, queriesWithError);

        int logs = LogUtil.countLogsContaining(log, "Email sent.");
        assertEquals(1, logs);
    }

    private List<AnalysisBean> getDefaultAnalysis() throws JSONException {
        List<AnalysisBean> analysisResult = new ArrayList<>();

        int topics = 1 + random.nextInt(MAX_TOPICS_FOUND);

        for (int i = 0; i < topics; i++) {
            AnalysisBean analysisBean = new AnalysisBean();

            SanityCheck sanityCheck = StubUtil.getDefaultSanityCheck(i);
            analysisBean.setSanityCheck(sanityCheck);

            int inconsistencies = 1 + random.nextInt(MAX_INCONSISTENCIES_FOUND);

            List<JSONObject> analysis = new ArrayList<>();
            for (int j = 0; j < inconsistencies; j++) {
                JSONObject jsonObject = new JSONObject();
                int columnsFound = 1 + random.nextInt(MAX_COLUMNS_FOUND);
                for (int k = 0; k < columnsFound; k++) {
                    jsonObject.put("col " + k, "result " + k);
                }
                analysis.add(jsonObject);
            }
            analysisBean.setAnalysis(analysis);
            analysisResult.add(analysisBean);
        }

        return analysisResult;
    }

    private List<SanityCheckWithErrorBean> getDefaultQueriesWithError() {
        List<SanityCheckWithErrorBean> result = new ArrayList<>();

        int topics = 1 + random.nextInt(MAX_TOPICS_FOUND);

        for (int i = 0; i < topics; i++) {
            SanityCheckWithErrorBean queryWithError = new SanityCheckWithErrorBean();

            SanityCheck sanityCheck = StubUtil.getDefaultSanityCheck(i);

            queryWithError.setSanityCheck(sanityCheck);
            queryWithError.setError("Error " + i);

            result.add(queryWithError);
        }

        return result;
    }

}
