package org.worldcubeassociation.dbsanitycheck.tasklet;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.worldcubeassociation.dbsanitycheck.bean.QueryBean;
import org.worldcubeassociation.dbsanitycheck.exception.SanityCheckException;
import org.worldcubeassociation.dbsanitycheck.reader.QueryReader;
import org.worldcubeassociation.dbsanitycheck.service.EmailService;

public class WrtSanityCheckTaskletTest {

	@InjectMocks
	private WrtSanityCheckTasklet wrtSanityCheckTasklet;

	@Mock
	private StepContribution contribution;

	@Mock
	private ChunkContext chunkContext;

	@Mock
	private QueryReader queryReader;

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
	public void executeTest() throws FileNotFoundException, SanityCheckException, MessagingException {
		when(queryReader.read()).thenReturn(getDefaultQueries());
		when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenAnswer(answer -> getDefaultQueryResult());

		RepeatStatus status = wrtSanityCheckTasklet.execute(contribution, chunkContext);

		assertEquals(RepeatStatus.FINISHED, status);
	}

	private List<Map<String, String>> getDefaultQueryResult() {
		List<Map<String, String>> result = new ArrayList<>();

		int numberOfResults = random.nextInt(MAX_QUERY_RESULT);
		int numberOfColumns = 1 + random.nextInt(MAX_NUMBER_OF_COLUMNS);
		for (int i = 0; i < numberOfResults; i++) {
			Map<String, String> map = new LinkedHashMap<>();
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

	private List<QueryBean> getDefaultQueries() {
		List<QueryBean> result = new ArrayList<>();

		int categories = 1 + random.nextInt(MAX_CATEGORIES);
		int query = 0;

		for (int i = 0; i < categories; i++) {
			int topics = 1 + random.nextInt(MAX_TOPICS);
			for (int j = 0; j < topics; j++) {
				QueryBean queryBean = new QueryBean();
				queryBean.setCategory("Category " + i);
				queryBean.setTopic("Topic " + j);
				queryBean.setQuery("Query " + (query++));

				result.add(queryBean);
			}
		}

		return result;
	}

}
