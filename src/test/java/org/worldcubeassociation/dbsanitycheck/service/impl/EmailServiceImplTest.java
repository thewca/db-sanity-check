package org.worldcubeassociation.dbsanitycheck.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.mail.MessagingException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.util.ReflectionTestUtils;
import org.worldcubeassociation.dbsanitycheck.bean.AnalysisBean;
import org.worldcubeassociation.dbsanitycheck.bean.QueryWithErrorBean;
import org.worldcubeassociation.dbsanitycheck.util.LogUtil;

import ch.qos.logback.classic.Logger;

public class EmailServiceImplTest {

	@InjectMocks
	private EmailServiceImpl emailService;

	@Mock
	private JavaMailSender emailSender;

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
	public void sendEmailTest() throws MessagingException {
		ReflectionTestUtils.setField(emailService, "sendMail", true);

		Logger log = LogUtil.getDefaultLogger(EmailServiceImpl.class);

		JavaMailSenderImpl sender = new JavaMailSenderImpl();
		when(emailSender.createMimeMessage()).thenReturn(sender.createMimeMessage());

		List<AnalysisBean> analysisResult = getDefaultAnalysis();
		List<QueryWithErrorBean> queriesWithError = new ArrayList<>();

		emailService.sendEmail(analysisResult, queriesWithError);

		int logs = LogUtil.countLogsContaining(log, "Email sent.");
		assertEquals(1, logs);
	}

	@Test
	public void sendEmailFalseTest() throws MessagingException {
		ReflectionTestUtils.setField(emailService, "sendMail", false);

		Logger log = LogUtil.getDefaultLogger(EmailServiceImpl.class);

		List<AnalysisBean> analysisResult = getDefaultAnalysis();
		List<QueryWithErrorBean> queriesWithError = new ArrayList<>();

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
		List<QueryWithErrorBean> queriesWithError = new ArrayList<>();

		emailService.sendEmail(analysisResult, queriesWithError);

		int logs = LogUtil.countLogsContaining(log, "Email sent.");
		assertEquals(1, logs);
	}

	private List<AnalysisBean> getDefaultAnalysis() {
		List<AnalysisBean> analysisResult = new ArrayList<>();

		int topics = 1 + random.nextInt(MAX_TOPICS_FOUND);

		for (int i = 0; i < topics; i++) {
			AnalysisBean analysisBean = new AnalysisBean();
			analysisBean.setCategory("Category " + i);
			analysisBean.setTopic("Topic " + i);

			int inconsistencies = 1 + random.nextInt(MAX_INCONSISTENCIES_FOUND);

			List<Map<String, String>> analysis = new ArrayList<>();
			for (int j = 0; j < inconsistencies; j++) {
				Map<String, String> map = new LinkedHashMap<>();
				int columnsFound = 1 + random.nextInt(MAX_COLUMNS_FOUND);
				for (int k = 0; k < columnsFound; k++) {
					map.put("col " + k, "result " + k);
				}
				analysis.add(map);
			}
			analysisBean.setAnalysis(analysis);
			analysisResult.add(analysisBean);
		}

		return analysisResult;
	}

}
