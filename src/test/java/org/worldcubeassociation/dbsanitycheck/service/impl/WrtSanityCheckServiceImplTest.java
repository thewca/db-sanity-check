package org.worldcubeassociation.dbsanitycheck.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.mail.MessagingException;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.worldcubeassociation.dbsanitycheck.exception.SanityCheckException;
import org.worldcubeassociation.dbsanitycheck.model.SanityCheck;
import org.worldcubeassociation.dbsanitycheck.model.SanityCheckCategory;
import org.worldcubeassociation.dbsanitycheck.repository.SanityCheckRepository;
import org.worldcubeassociation.dbsanitycheck.service.EmailService;
import org.worldcubeassociation.dbsanitycheck.util.LogUtil;

import ch.qos.logback.classic.Logger;

public class WrtSanityCheckServiceImplTest {

	@InjectMocks
	private WrtSanityCheckServiceImpl wrtSanityCheckTasklet;

	@Mock
	private SanityCheckRepository sanityCheckRepository;

	@Mock
	private EmailService emailService;

	@Mock
	private JdbcTemplate jdbcTemplate;

	private static final Random random = new Random();

	private static final int MAX_CATEGORIES = 10;
	private static final int MAX_TOPICS = 5;
	private static final int MAX_QUERY_RESULT = 3;
	private static final int MAX_NUMBER_OF_COLUMNS = 12;
	private static final int MAX_RESULT_LENGTH = 20;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void bestCaseScenarioTest() throws MessagingException {
		Logger log = LogUtil.getDefaultLogger(WrtSanityCheckServiceImpl.class);

		when(sanityCheckRepository.findAll(any(Sort.class))).thenReturn(getDefaultSanityChecks());
		when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenAnswer(answer -> getDefaultQueryResult());

		wrtSanityCheckTasklet.execute();

		int logs = LogUtil.countLogsContaining(log, "Sanity check finished");
		assertEquals(1, logs);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void queryWithErrorTest() {
		Logger log = LogUtil.getDefaultLogger(WrtSanityCheckServiceImpl.class);

		BadSqlGrammarException exception = new BadSqlGrammarException(null, "Select * from A inner join B on", null);

		//when(queryReader.read()).thenReturn(getDefaultSanityChecks());
		when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenThrow(exception);

		//wrtSanityCheckTasklet.execute();

		// There should be some warning about bad grammar
		List<String> logs = LogUtil.getLogsContaining(log, "Could not execute the query");
		assertFalse(logs.isEmpty());
	}

	private List<JSONObject> getDefaultQueryResult() throws JSONException {
		List<JSONObject> result = new ArrayList<>();

		int numberOfResults = random.nextInt(MAX_QUERY_RESULT);
		int numberOfColumns = 1 + random.nextInt(MAX_NUMBER_OF_COLUMNS);
		for (int i = 0; i < numberOfResults; i++) {
			JSONObject map = new JSONObject();
			for (int j = 0; j < numberOfColumns; j++) {
				map.put("Col " + j, getRandomResult());
			}
			result.add(map);
		}

		return result;
	}

	private String getRandomResult() {
		String result = "";
		for (int i = 0; i < random.nextInt(MAX_RESULT_LENGTH); i++) {
			result += (char) (65 + random.nextInt(26));
		}
		return result;
	}

	private List<SanityCheck> getDefaultSanityChecks() {
		List<SanityCheck> result = new ArrayList<>();

		int categories = 1 + random.nextInt(MAX_CATEGORIES);

		for (int i = 0; i < categories; i++) {
			int topics = 1 + random.nextInt(MAX_TOPICS);

			SanityCheckCategory sanityCheckCategory = new SanityCheckCategory();
			sanityCheckCategory.setId(i);
			sanityCheckCategory.setName("Category " + i);
			for (int j = 0; j < topics; j++) {
				int id = i * topics + j;
				SanityCheck sanityCheck = new SanityCheck();
				sanityCheck.setId(id);
				sanityCheck.setQuery("Query " + id);
				sanityCheck.setExclusions(new ArrayList<>());
				sanityCheck.setTopic("Topic " + id);
				sanityCheck.setSanityCheckCategoryId(i);
				sanityCheck.setSanityCheckCategory(sanityCheckCategory);
				sanityCheck.setComments("Comment " + id);

				result.add(sanityCheck);
			}
		}

		return result;
	}

}
