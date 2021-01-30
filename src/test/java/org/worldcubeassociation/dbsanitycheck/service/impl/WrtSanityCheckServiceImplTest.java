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
import org.worldcubeassociation.dbsanitycheck.model.Category;
import org.worldcubeassociation.dbsanitycheck.model.Exclusion;
import org.worldcubeassociation.dbsanitycheck.repository.SanityCheckRepository;
import org.worldcubeassociation.dbsanitycheck.service.EmailService;
import org.worldcubeassociation.dbsanitycheck.util.LogUtil;
import org.worldcubeassociation.dbsanitycheck.util.StubUtil;

import java.time.LocalDate;
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

    @Test
    public void allResultsFalsePositivesTest() throws MessagingException, JSONException {
        Logger log = LogUtil.getDefaultLogger(WrtSanityCheckServiceImpl.class);

        List<SanityCheck> sanityChecks = new ArrayList<>();
        SanityCheck sanityCheck = new SanityCheck();
        sanityCheck.setTopic("Topic 1");
        sanityCheck.setQuery("select * from Results");

        Category category = new Category();
        category.setName("Category");
        sanityCheck.setCategory(category);

        sanityChecks.add(sanityCheck);

        LocalDate date = LocalDate.now();

        int nResults = 10;

        List<Exclusion> exclusions = new ArrayList<>();
        Exclusion exclusion = new Exclusion();

        // The exclusion json will be like the result below, but with less columns
        // 1 exclusion for them all
        JSONObject json = new JSONObject();
        json.put("country", "Brazil");
        json.put("competitionId", "WC" + date.getYear());

        // As of now, sanity checks stores exclusion as a dumped json
        exclusion.setExclusion(json.toString());
        exclusions.add(exclusion);
        sanityCheck.setExclusions(exclusions);

        List<JSONObject> queryResult = new ArrayList<>();

        // This will be a json found by the sanity check
        for (int i = 0; i < nResults; i++) {
            JSONObject jsonResult = new JSONObject();
            jsonResult.put("id", i);
            jsonResult.put("personName", "Person " + i);
            jsonResult.put("competitionId", "WC" + date.getYear());
            jsonResult.put("country", "Brazil");
            queryResult.add(jsonResult);
        }

        when(sanityCheckRepository.findAll(any(Sort.class))).thenReturn(sanityChecks);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
                .thenAnswer(answer -> queryResult);

        wrtSanityCheckTasklet.execute();

        int logs = LogUtil.countLogsContaining(log, "All the results are known false positives");
        assertEquals(1, logs);
    }

    @Test
    public void noResultExcludedDespiteHavingExclusionTest() throws MessagingException, JSONException {
        Logger log = LogUtil.getDefaultLogger(WrtSanityCheckServiceImpl.class);

        List<SanityCheck> sanityChecks = new ArrayList<>();
        SanityCheck sanityCheck = new SanityCheck();
        sanityCheck.setTopic("Topic 1");
        sanityCheck.setQuery("select * from Results");

        Category category = new Category();
        category.setName("Category");
        sanityCheck.setCategory(category);

        sanityChecks.add(sanityCheck);

        LocalDate date = LocalDate.now();

        int nResults = 10;

        List<Exclusion> exclusions = new ArrayList<>();
        Exclusion exclusion = new Exclusion();

        // The exclusion json will be like the result below, but with less columns
        JSONObject json = new JSONObject();
        json.put("country", "Brazil");
        json.put("competitionId", "WCA" + date.getYear()); // Slightly different, not excluded

        // As of now, sanity checks stores exclusion as a dumped json
        exclusion.setExclusion(json.toString());
        exclusions.add(exclusion);
        sanityCheck.setExclusions(exclusions);

        List<JSONObject> queryResult = new ArrayList<>();

        // This will be a json found by the sanity check
        for (int i = 0; i < nResults; i++) {
            JSONObject jsonResult = new JSONObject();
            jsonResult.put("id", i);
            jsonResult.put("personName", "Person " + i);
            jsonResult.put("competitionId", "WC" + date.getYear());
            jsonResult.put("country", "Brazil");
            queryResult.add(jsonResult);
        }

        when(sanityCheckRepository.findAll(any(Sort.class))).thenReturn(sanityChecks);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
                .thenAnswer(answer -> queryResult);

        wrtSanityCheckTasklet.execute();

        int logs = LogUtil.countLogsContaining(log,
                "There are false positives in the database, but no results were excluded");
        assertEquals(1, logs);
    }

    @Test
    public void someResultExcludedTest() throws MessagingException, JSONException {
        Logger log = LogUtil.getDefaultLogger(WrtSanityCheckServiceImpl.class);

        List<SanityCheck> sanityChecks = new ArrayList<>();
        SanityCheck sanityCheck = new SanityCheck();
        sanityCheck.setTopic("Topic 1");
        sanityCheck.setQuery("select * from Results");

        Category category = new Category();
        category.setName("Category");
        sanityCheck.setCategory(category);

        sanityChecks.add(sanityCheck);

        LocalDate date = LocalDate.now();

        int nResults = 10;

        List<Exclusion> exclusions = new ArrayList<>();
        Exclusion exclusion = new Exclusion();

        // The exclusion json will be like the result below, but with less columns
        JSONObject json = new JSONObject();
        json.put("country", "Brazil");
        json.put("competitionId", "WC" + date.getYear());

        // As of now, sanity checks stores exclusion as a dumped json
        exclusion.setExclusion(json.toString());
        exclusions.add(exclusion);
        sanityCheck.setExclusions(exclusions);

        List<JSONObject> queryResult = new ArrayList<>();

        // This will be a json found by the sanity check
        for (int i = 0; i < nResults; i++) {
            JSONObject jsonResult = new JSONObject();
            jsonResult.put("id", i);
            jsonResult.put("personName", "Person " + i);
            jsonResult
                    .put("competitionId", "WC" + (date.getYear() + (i > 5 ? 1 : 0))); // This will differ in some cases
            jsonResult.put("country", "Brazil");
            queryResult.add(jsonResult);
        }

        when(sanityCheckRepository.findAll(any(Sort.class))).thenReturn(sanityChecks);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
                .thenAnswer(answer -> queryResult);

        wrtSanityCheckTasklet.execute();

        int logs = LogUtil.countLogsContaining(log,
                "There are some false positives, but not all the results were false positives");
        assertEquals(1, logs);
    }

    @Test
    public void moreExclusionThanNeededTest() throws MessagingException, JSONException {
        Logger log = LogUtil.getDefaultLogger(WrtSanityCheckServiceImpl.class);

        List<SanityCheck> sanityChecks = new ArrayList<>();
        SanityCheck sanityCheck = new SanityCheck();
        sanityCheck.setTopic("Topic 1");
        sanityCheck.setQuery("select * from Results");

        Category category = new Category();
        category.setName("Category");
        sanityCheck.setCategory(category);

        sanityChecks.add(sanityCheck);

        LocalDate date = LocalDate.now();

        int nResults = 10;

        List<Exclusion> exclusions = new ArrayList<>();
        for (int i = 0; i < nResults + 1; i++) {
            Exclusion exclusion = new Exclusion();

            // The exclusion json will be like the result below, but with less columns
            JSONObject json = new JSONObject();
            json.put("country", "Brazil");
            json.put("competitionId", "WC" + date.getYear());

            // As of now, sanity checks stores exclusion as a dumped json
            exclusion.setExclusion(json.toString());
            exclusions.add(exclusion);
        }
        sanityCheck.setExclusions(exclusions);

        List<JSONObject> queryResult = new ArrayList<>();

        // This will be a json found by the sanity check
        for (int i = 0; i < nResults; i++) {
            JSONObject jsonResult = new JSONObject();
            jsonResult.put("id", i);
            jsonResult.put("personName", "Person " + i);
            jsonResult
                    .put("competitionId", "WC" + (date.getYear() + (i > 5 ? 1 : 0))); // This will differ in some cases
            jsonResult.put("country", "Brazil");
            queryResult.add(jsonResult);
        }

        when(sanityCheckRepository.findAll(any(Sort.class))).thenReturn(sanityChecks);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
                .thenAnswer(answer -> queryResult);

        wrtSanityCheckTasklet.execute();

        int logs = LogUtil.countLogsContaining(log, "You have more exclusions than results, check the exclusions");
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

            Category category = StubUtil.getDefaultSanityCheckCategory(i);
            for (int j = 0; j < topics; j++) {
                int id = i * topics + j;
                SanityCheck sanityCheck = StubUtil.getDefaultSanityCheck(category, id);

                result.add(sanityCheck);
            }
        }
        return result;
    }

}
