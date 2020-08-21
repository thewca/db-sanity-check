package org.worldcubeassociation.dbsanitycheck.tasklet;

import java.io.FileNotFoundException;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.worldcubeassociation.dbsanitycheck.bean.QueryBean;
import org.worldcubeassociation.dbsanitycheck.exception.SanityCheckException;
import org.worldcubeassociation.dbsanitycheck.reader.QueryReader;
import org.worldcubeassociation.dbsanitycheck.service.EmailService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WrtSanityCheckTasklet implements Tasklet {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private QueryReader queryReader;

	@Autowired
	private EmailService emailService;

	// Hold inconsistencies
	private Map<String, List<String>> analysis = new LinkedHashMap<>();

	private List<QueryBean> queries = new ArrayList<>();

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
			throws FileNotFoundException, SanityCheckException, MessagingException {

		// Read queryes
		queries = queryReader.read();

		executeQueries();
		showResults();

		log.info("All queries executed");

		emailService.sendEmail(analysis);

		return RepeatStatus.FINISHED;
	}

	private void executeQueries() {
		log.info("Execute queries");

		String prevCategory = null;
		for (QueryBean query : queries) {

			// We log at each new category
			String category = query.getCategory();
			if (prevCategory == null || !prevCategory.equals(category)) {
				log.info(" ========== Category = {} ========== ", category);
				prevCategory = category;
			}

			generalAnalysis(query);
		}
	}

	/**
	 * A general purpose analysis. If the query returns any value, it will be added
	 * to the result
	 * 
	 * @param topic defines the current operation
	 * @param query is the sql query
	 */
	private void generalAnalysis(QueryBean queryBean) {
		String category = queryBean.getCategory();
		String topic = queryBean.getTopic();
		String query = queryBean.getQuery();

		log.info(" ===== " + topic + " ===== ");
		log.info(query);

		List<String> result = jdbcTemplate.query(query, (rs, rowNum) -> {
			// Makes the result set into 1 result
			List<String> out = new ArrayList<>();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();

			// The column count starts from 1
			for (int i = 1; i <= columnCount; i++) {
				String name = rsmd.getColumnName(i);
				out.add(name + "=" + rs.getString(i));
			}
			return String.join(", ", out);
		});

		if (!result.isEmpty()) {
			log.info("* Found {} results for {}", result.size(), topic);
			analysis.put(String.format("[category] %s, [topic] %s", category, topic), result);
		}
	}

	private void showResults() {
		for (String key : analysis.keySet()) {
			log.warn(" ** Inconsistency at " + key);
			for (String result : analysis.get(key)) {
				log.info(result);
			}
		}
	}

}
