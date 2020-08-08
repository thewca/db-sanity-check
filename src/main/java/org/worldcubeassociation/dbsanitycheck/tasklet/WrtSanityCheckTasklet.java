package org.worldcubeassociation.dbsanitycheck.tasklet;

import java.io.IOException;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.worldcubeassociation.dbsanitycheck.bean.CategoryBean;
import org.worldcubeassociation.dbsanitycheck.exception.SanityCheckException;
import org.worldcubeassociation.dbsanitycheck.helper.QueryHelper;

import lombok.extern.slf4j.Slf4j;

// All the topics and queries here were provided by the WRT

// TODO consider false positives

@Slf4j
@Component
public class WrtSanityCheckTasklet implements Tasklet {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private QueryHelper queryHelper = new QueryHelper();

	// Hold category, topic and result
	private Map<String, List<String>> map = new HashMap<>();

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
			throws DataAccessException, IOException, SQLException, SanityCheckException {

		fillQueries();
		log.info("{} queries found", queryHelper.size());

		executeQueries();
		showResults();

		return RepeatStatus.FINISHED;
	}

	private void fillQueries() throws SanityCheckException {
		// This method is intended to be easily maintainable also by people with no java
		// skills
		String category;
		String topic;
		String query;
		
		category = "Person data irregularities";
		topic = "N1. Names with numbers";
		query = "SELECT * FROM Persons WHERE name REGEXP '[0-9]'";
		queryHelper.add(category, topic, query);
		
	}

	private void executeQueries() {
		log.info("Execute queries");

		 queryHelper.getCategories().keySet().forEach(cat -> {
			 log.info(" ========== Category = {} ========== ", cat);
			 
			 CategoryBean category = queryHelper.getQueriesByCategory(cat);
			 
			 category.getQueries().forEach(queryBean -> {
				 String topic = queryBean.getTopic();
				 String query = queryBean.getQuery();
				 log.info(" ===== Topic = {} ===== ", topic);
				 log.info("Query = {}", query);

				 generalAnalysis(topic, query);
			 });
		 });
	}

	/**
	 * A general purpose analysis. If the query returns any value, it will be added
	 * to the map
	 * 
	 * @param topic defines the current operation
	 * @param query is the sql query
	 */
	private void generalAnalysis(String topic, String query) {
		log.info("=== " + topic + " ===");
		log.info(query);

		List<String> result = jdbcTemplate.query(query, (rs, rowNum) -> {
			// Makes the result set into 1 result
			List<String> out = new ArrayList<>();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();

			// The column count starts from 1
			for (int i = 1; i <= columnCount; i++) {
				String name = rsmd.getColumnName(i);
				out.add(name + "=" + rs.getString(name));
			}
			return String.join(",", out);
		});

		if (!result.isEmpty()) {
			log.info("Found {} results for {}", result.size(), topic);
			map.put(topic, result);
		}
	}

	private void showResults() {
		for (String key : map.keySet()) {
			log.warn("Inconsistency in the topic " + key);
			for (String result : map.get(key)) {
				log.info(result);
			}
		}

	}

}
