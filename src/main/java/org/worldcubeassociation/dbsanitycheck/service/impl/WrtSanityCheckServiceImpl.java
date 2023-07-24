package org.worldcubeassociation.dbsanitycheck.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.worldcubeassociation.dbsanitycheck.bean.AnalysisBean;
import org.worldcubeassociation.dbsanitycheck.bean.SanityCheckWithErrorBean;
import org.worldcubeassociation.dbsanitycheck.model.Exclusion;
import org.worldcubeassociation.dbsanitycheck.model.SanityCheck;
import org.worldcubeassociation.dbsanitycheck.repository.SanityCheckRepository;
import org.worldcubeassociation.dbsanitycheck.service.EmailService;
import org.worldcubeassociation.dbsanitycheck.service.WrtSanityCheckService;

import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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


    @Override
    public void execute() throws MessagingException {
        log.info("WRT Sanity Check");

        log.info("Reading queries");

        List<SanityCheck> sanityChecks = sanityCheckRepository
                .findAll(Sort.by(Sort.Direction.ASC, "sanityCheckCategoryId", "topic"));
        log.info("Found {} queries", sanityChecks.size());

        Map<String, List<SanityCheck>> sanityChecksByEmail = sanityChecks.stream()
                .collect(Collectors.groupingBy(s -> Optional.ofNullable(s.getCategory().getEmailTo()).orElse("")));
        log.info("Found {} emails", sanityChecksByEmail.size());

        for (Map.Entry<String, List<SanityCheck>> entry : sanityChecksByEmail.entrySet()) {
            sendSanityChecksToEmail(entry.getKey(), entry.getValue());
        }
    }

    private void sendSanityChecksToEmail(String email, List<SanityCheck> sanityChecks) throws MessagingException {

        // Hold inconsistencies
        List<AnalysisBean> analysisResult = new ArrayList<>();

        List<SanityCheckWithErrorBean> queriesWithError = new ArrayList<>();

        executeSanityChecks(sanityChecks, analysisResult, queriesWithError);
        showResults(analysisResult, queriesWithError);

        log.info("All queries executed");

        emailService.sendEmail(email, analysisResult, queriesWithError);

        log.info("Sanity check finished");
    }

    private void executeSanityChecks(List<SanityCheck> sanityChecks, List<AnalysisBean> analysisResult,
                                     List<SanityCheckWithErrorBean> queriesWithError) {
        log.info("Execute queries");

        String prevCategory = null;
        for (SanityCheck sanityCheck : sanityChecks) {

            // We log at each new category
            String category = sanityCheck.getCategory().getName();
            if (prevCategory == null || !prevCategory.equals(category)) {
                log.info(" ========== Category = {} ========== ", category);
                prevCategory = category;
            }

            generalAnalysis(sanityCheck, analysisResult, queriesWithError);
        }
    }

    /**
     * A general purpose analysis. If the query returns any value, it will be added to the result
     */
    private void generalAnalysis(SanityCheck sanityCheck, List<AnalysisBean> analysisResult,
                                 List<SanityCheckWithErrorBean> queriesWithError) {
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

                List<String> columnsConsidered = new ArrayList<>();

                // The column count starts from 1
                for (int i = 1; i <= columnCount; i++) {
                    String name = rsmd.getColumnLabel(i);

                    if (columnsConsidered.contains(name)) {
                        throw new RuntimeException("Column " + name + " is duplicated");
                    }

                    columnsConsidered.add(name);
                    out.put(name, rs.getString(i));
                }
                return out;
            });
            log.info("Found {} results for {}", result.size(), topic);

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
            AnalysisBean analysisBean = new AnalysisBean();
            analysisBean.setSanityCheck(sanityCheck);
            analysisBean.setAnalysis(result);

            analysisResult.add(analysisBean);
        }
    }

    private void removeExclusions(List<JSONObject> result, SanityCheck sanityCheck) {
        List<JSONObject> exclusions = sanityCheck.getExclusions().stream().map(Exclusion::getExclusion)
                .map(JSONObject::new).collect(Collectors.toList());

        if (exclusions.isEmpty()) {
            return;
        }

        log.info("{} exclusions found", sanityCheck.getExclusions().size());

        List<JSONObject> remains = result.stream().filter(it -> !compareExistingKeys(it, exclusions))
                .collect(Collectors.toList());

        if (exclusions.size() > result.size()) {
            log.info("You have more exclusions than results, check the exclusions.");
        }

        if (remains.isEmpty()) {
            log.info("All the results are known false positives");
        } else if (remains.size() == result.size()) {
            log.info(
                    "There are false positives in the database, but no results were excluded, please check the "
                            + "exclusion");
        } else {
            log.info("There are some false positives, but not all the results were false positives");
        }

        // This is result = remains, but without loosing reference
        result.clear();
        result.addAll(remains);

        log.info("Remaining size: {}", result.size());
    }

    // Returns true if one of the exclusions matches sanity check result
    // Considers
    private boolean compareExistingKeys(JSONObject sanityCheckResult, List<JSONObject> exclusions) {

        for (JSONObject exclusion : exclusions) {
            if (partiallyEquals(exclusion, sanityCheckResult)) {
                return true;
            }
        }
        return false;
    }

    // If existing keys of exclusion matches exclusion keys of sanityCheckResult, returns true.
    // Note that this can let solumn keys of sanityCheckResult out of the comparison
    private boolean partiallyEquals(JSONObject exclusion, JSONObject sanityCheckResult) {
        Iterator<String> keys = exclusion.keys();

        while (keys.hasNext()) {
            String key = keys.next();

            if (!exclusion.optString(key).equals(sanityCheckResult.optString(key))) {
                return false;
            }
        }

        return true;
    }


    private void showResults(List<AnalysisBean> analysisResult, List<SanityCheckWithErrorBean> queriesWithError) {
        analysisResult.forEach(item -> {
            log.warn(" ** Inconsistency at [{}] {}", item.getSanityCheck().getCategory().getName(),
                    item.getSanityCheck().getTopic());
            item.getAnalysis().forEach(it -> log.info(it.toString()));
        });

        queriesWithError.forEach(item -> {
            log.warn(" ** Query with error in [{}] {}", item.getSanityCheck().getCategory().getName(),
                    item.getSanityCheck().getTopic());
            log.warn(item.getError());
        });
    }
}
