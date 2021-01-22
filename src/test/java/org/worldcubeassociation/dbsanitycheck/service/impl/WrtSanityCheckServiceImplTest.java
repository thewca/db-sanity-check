package org.worldcubeassociation.dbsanitycheck.service.impl;

import ch.qos.logback.classic.Logger;
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
import org.worldcubeassociation.dbsanitycheck.model.SanityCheck;
import org.worldcubeassociation.dbsanitycheck.model.SanityCheckCategory;
import org.worldcubeassociation.dbsanitycheck.repository.SanityCheckRepository;
import org.worldcubeassociation.dbsanitycheck.service.EmailService;
import org.worldcubeassociation.dbsanitycheck.util.LogUtil;
import org.worldcubeassociation.dbsanitycheck.util.StubUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import javax.mail.MessagingException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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
    public void bestCaseScenarioTest() throws MessagingException, JSONException {
        Logger log = LogUtil.getDefaultLogger(WrtSanityCheckServiceImpl.class);

        List<SanityCheck> defaultSanityChecks = getDefaultSanityChecks();
        List<List<JSONObject>> defaultQueryResult = getDefaultQueryResult(defaultSanityChecks);

        AtomicInteger count = new AtomicInteger();

        when(sanityCheckRepository.findAll(any(Sort.class))).thenReturn(defaultSanityChecks);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
                .thenAnswer(answer -> defaultQueryResult.get(count.getAndIncrement()));

        wrtSanityCheckTasklet.execute();

        int logs = LogUtil.countLogsContaining(log, "Sanity check finished");
        assertEquals(1, logs);
    }

    @Test
    public void queryWithErrorTest() throws MessagingException {
        Logger log = LogUtil.getDefaultLogger(WrtSanityCheckServiceImpl.class);

        BadSqlGrammarException exception = new BadSqlGrammarException(null, "Select * from A inner join B on", null);

        when(sanityCheckRepository.findAll(any(Sort.class))).thenReturn(getDefaultSanityChecks());
        when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenThrow(exception);

        wrtSanityCheckTasklet.execute();

        // There should be some warning about bad grammar
        List<String> logs = LogUtil.getLogsContaining(log, "Could not execute the query");
        assertFalse(logs.isEmpty());
    }

    @Test
    public void exclusionTest() throws MessagingException, JSONException {
        Logger log = LogUtil.getDefaultLogger(WrtSanityCheckServiceImpl.class);

        List<SanityCheck> defaultSanityChecks = getDefaultSanityChecks();
        List<List<JSONObject>> defaultQueryResult = getDefaultQueryResult(defaultSanityChecks);

        AtomicInteger count = new AtomicInteger();

        when(sanityCheckRepository.findAll(any(Sort.class))).thenReturn(defaultSanityChecks);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
                .thenAnswer(answer -> defaultQueryResult.get(count.getAndIncrement()));

        wrtSanityCheckTasklet.execute();

        int logs = LogUtil.countLogsContaining(log, "Sanity check finished");
        assertEquals(1, logs);
    }

    private List<List<JSONObject>> getDefaultQueryResult(List<SanityCheck> sanityChecks) throws JSONException {
        List<List<JSONObject>> result = new ArrayList<>();

        for (int i = 0; i < sanityChecks.size(); i++) {
            List<JSONObject> list = new ArrayList<>();

            int numberOfResults = random.nextInt(MAX_RESULT_LENGTH);
            for (int j = 0; j < numberOfResults; j++) {
                int numberOfColumns = 1 + random.nextInt(MAX_NUMBER_OF_COLUMNS);
                JSONObject json = new JSONObject();
                for (int k = 0; k < numberOfColumns; k++) {
                    json.put("Col " + k, getRandomResult());
                }
                list.add(json);
            }
            result.add(list);
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

            SanityCheckCategory sanityCheckCategory = StubUtil.getDefaultSanityCheckCategory(i);
            for (int j = 0; j < topics; j++) {
                int id = i * topics + j;
                SanityCheck sanityCheck = StubUtil.getDefaultSanityCheck(sanityCheckCategory, id);

                result.add(sanityCheck);
            }
        }
        return result;
    }

}
