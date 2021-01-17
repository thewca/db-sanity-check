package org.worldcubeassociation.dbsanitycheck.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.worldcubeassociation.dbsanitycheck.bean.AnalysisBean;
import org.worldcubeassociation.dbsanitycheck.bean.SanityCheckWithErrorBean;
import org.worldcubeassociation.dbsanitycheck.model.SanityCheck;
import org.worldcubeassociation.dbsanitycheck.model.SanityCheckExclusion;
import org.worldcubeassociation.dbsanitycheck.repository.SanityCheckRepository;
import org.worldcubeassociation.dbsanitycheck.service.EmailService;
import org.worldcubeassociation.dbsanitycheck.service.WrtSanityCheckService;

import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.mail.MessagingException;

@Slf4j
@Component
public class WrtSanityCheckServiceImpl implements WrtSanityCheckService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SanityCheckRepository sanityCheckRepository;

    // Hold inconsistencies
    private List<AnalysisBean> analysisResult = new ArrayList<>();

    private List<SanityCheckWithErrorBean> queriesWithError = new ArrayList<>();

    private static final List<Integer> IGNORE = Arrays.asList(11, 18, 20, 21, 24);

    @Override
    public void execute() throws MessagingException {
        log.info("WRT Sanity Check");

        log.info("Reading queries");

        // Read queryes
        List<SanityCheck> sanityChecks = sanityCheckRepository
                .findAll(Sort.by(Sort.Direction.ASC, "sanityCheckCategoryId", "topic"));
        log.info("Found {} queries", sanityChecks.size());

        executeSanityChecks(sanityChecks);
        showResults();

        log.info("All queries executed");

        emailService.sendEmail(analysisResult, queriesWithError);

        log.info("Sanity check finished");
    }

    private void executeSanityChecks(List<SanityCheck> sanityChecks) {
        log.info("Execute queries");

        String prevCategory = null;
        for (SanityCheck sanityCheck : sanityChecks) {

            // TODO remove, dev speed
            if (sanityCheck.getExclusions().isEmpty() || IGNORE.contains(sanityCheck.getId())) {
                continue;
            }

            // We log at each new category
            String category = sanityCheck.getSanityCheckCategory().getName();
            if (prevCategory == null || !prevCategory.equals(category)) {
                log.info(" ========== Category = {} ========== ", category);
                prevCategory = category;
            }

            generalAnalysis(sanityCheck);
        }
    }

    /**
     * A general purpose analysis. If the query returns any value, it will be added to the result
     *
     * @param sanityCheck
     */
    private void generalAnalysis(SanityCheck sanityCheck) {
        String topic = sanityCheck.getTopic();
        String query = sanityCheck.getQuery();

        List<JSONObject> result;
        try {

            log.info(" ===== " + topic + " ===== ");
            log.info(query);

            result = jdbcTemplate.query(query, (rs, rowNum) -> {
                // Makes the result set into 1 result
                JSONObject out = new JSONObject();
                ResultSetMetaData rsmd = rs.getMetaData();
                int columnCount = rsmd.getColumnCount();

                // The column count starts from 1
                for (int i = 1; i <= columnCount; i++) {
                    String name = rsmd.getColumnName(i);
                    out.put(name, rs.getString(i));
                }
                return out;
            });

            removeExclusions(result, sanityCheck);
        } catch (Exception e) {
            log.error("Could not execute the query {}\n{}", query, e.getMessage());
            SanityCheckWithErrorBean sanityCheckWithErrorBean = new SanityCheckWithErrorBean();
            sanityCheckWithErrorBean.setSanityCheck(sanityCheck);
            sanityCheckWithErrorBean.setError(e.getMessage());
            queriesWithError.add(sanityCheckWithErrorBean);
            return;
        }

        if (!result.isEmpty()) {
            log.info("* Found {} results for {}", result.size(), topic);

            AnalysisBean analysisBean = new AnalysisBean();
            analysisBean.setSanityCheck(sanityCheck);
            analysisBean.setAnalysis(result);

            analysisResult.add(analysisBean);
        }
    }

    private void removeExclusions(List<JSONObject> result, SanityCheck sanityCheck) {
        // We compare strings instead of json because comparing json in java is painful
        // String comparison is also a bad option, so we just do string -> json ->
        // string
        List<String> exclusions = sanityCheck.getExclusions().stream().map(SanityCheckExclusion::getExclusion)
                .map(JSONObject::new).map(Object::toString).collect(Collectors.toList());

        if (exclusions.isEmpty()) {
            return;
        }

        log.info("{} exclusions found", sanityCheck.getExclusions().size());

        List<JSONObject> remains = result.stream().filter(it -> !exclusions.contains(it.toString()))
                .collect(Collectors.toList());

        if (remains.isEmpty()) {
            log.info("All the results are known false positives");
        } else if (remains.size() == result.size()) {
            log.info(
                    "There are false positives in the database, but no result were excluded. Please check the "
                            + "exclusion.");
        } else {
            log.info("There are some false positives, but no all the results were false positives");
        }

        // This is result = remains, but without loosing reference
        result.clear();
        result.addAll(remains);
    }

    private void showResults() {
        analysisResult.forEach(item -> {
            log.warn(" ** Inconsistency at [{}] {}", item.getSanityCheck().getSanityCheckCategory().getName(),
                    item.getSanityCheck().getTopic());
            item.getAnalysis().stream().forEach(it -> log.info(it.toString()));
        });

        queriesWithError.forEach(item -> {
            log.warn(" ** Query with error in [{}] {}", item.getSanityCheck().getSanityCheckCategory().getName(),
                    item.getSanityCheck().getTopic());
            log.warn(item.getError());
        });
    }
}
