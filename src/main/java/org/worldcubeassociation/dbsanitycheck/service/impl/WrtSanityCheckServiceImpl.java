package org.worldcubeassociation.dbsanitycheck.service.impl;

import java.io.FileNotFoundException;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.worldcubeassociation.dbsanitycheck.bean.AnalysisBean;
import org.worldcubeassociation.dbsanitycheck.bean.SanityCheckWithErrorBean;
import org.worldcubeassociation.dbsanitycheck.exception.SanityCheckException;
import org.worldcubeassociation.dbsanitycheck.model.SanityCheck;
import org.worldcubeassociation.dbsanitycheck.repository.SanityCheckRepository;
import org.worldcubeassociation.dbsanitycheck.service.EmailService;
import org.worldcubeassociation.dbsanitycheck.service.WrtSanityCheckService;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

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

	private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	// TODO remove, dev, avoid long queries
	private static List<Long> IGNORE = List.of(11L, 18L, 20L, 21L, 24L);

	@Override
	public void execute() throws FileNotFoundException, SanityCheckException, MessagingException {
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

			// TODO remove, dev
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
	 * A general purpose analysis. If the query returns any value, it will be added
	 * to the result
	 * 
	 * @param topic defines the current operation
	 * @param query is the sql query
	 */
	private void generalAnalysis(SanityCheck sanityCheck) {
		String topic = sanityCheck.getTopic();
		String query = sanityCheck.getQuery();

		// With good constraints, this won't throw a null pointer
		String category = sanityCheck.getSanityCheckCategory().getName();

		List<Map<String, String>> result = new ArrayList<>();
		try {

			log.info(" ===== " + topic + " ===== ");
			log.info(query);

			if (!sanityCheck.getExclusions().isEmpty()) {
				log.info("{} exclusions found", sanityCheck.getExclusions().size());
			}

			result = jdbcTemplate.query(query, (rs, rowNum) -> {
				// Makes the result set into 1 result
				Map<String, String> out = new LinkedHashMap<>();
				ResultSetMetaData rsmd = rs.getMetaData();
				int columnCount = rsmd.getColumnCount();

				// The column count starts from 1
				for (int i = 1; i <= columnCount; i++) {
					String name = rsmd.getColumnName(i);
					out.put(name, rs.getString(i));
				}
				return out;
			});
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
			analysisBean.setCategory(category);
			analysisBean.setTopic(topic);
			analysisBean.setAnalysis(result);

			analysisResult.add(analysisBean);
		}
	}

	private void showResults() {
		analysisResult.forEach(item -> {
			log.warn(" ** Inconsistency at [{}] {}", item.getCategory(), item.getTopic());
			item.getAnalysis().stream().forEach(this::logMap);
		});
	}

	// LinkedHashMap has a nice toString. We just remove { and } from the edges.
	private void logMap(Map<String, String> analysis) {
		String str = analysis.toString();
		log.info(str.substring(1, str.length() - 1));
	}

}
