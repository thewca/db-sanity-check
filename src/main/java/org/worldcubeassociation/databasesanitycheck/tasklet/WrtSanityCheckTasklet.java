package org.worldcubeassociation.databasesanitycheck.tasklet;

import java.io.IOException;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

// All the topics and queries here were provided by the WRT

// TODO consider false positives

@Slf4j
@Component
public class WrtSanityCheckTasklet implements Tasklet {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Value("${job.min-accepted-age}")
	private int minAcceptedAge;

	@Value("${job.max-accepted-age}")
	private int maxAcceptedAge;

	@Value("${job.missing-dob-at-scale}")
	private int missingDOBAtScale;

	private int currentYear = LocalDate.now().getYear();

	private Map<String, List<String>> map = new HashMap<>();

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
			throws DataAccessException, IOException, SQLException {

		incorrectDOB();

		missingDOBAtScale();

		nameWithNumbers();
		lowerCaseFirstName();
		lowerCaseLastNameWithoutLocalName();
		lowerCaseLastNameWithLocalName();

		badLocalNames();
		missingOneBracketForLocalName();
		trailingSpaces();

		emptyGender();

		// TODO takes long in developing // inconsistentWcaId();

		personsTableEntriesEithoutResultsTableEntry();

		for (String key : map.keySet()) {
			log.warn("Inconsistency at " + key);
			for (String result : map.get(key)) {
				log.info(result);
			}
		}

		return RepeatStatus.FINISHED;
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

	private void incorrectDOB() {
		String topic = "Incorrect DOB";
		log.info(" === " + topic + " === ");

		int minAcceptedYear = currentYear - maxAcceptedAge;
		int maxAcceptedYear = currentYear - minAcceptedAge;
		log.info("Min accepted year is {}", minAcceptedYear);
		log.info("Max accepted year is {}", maxAcceptedYear);

		String query = "SELECT * FROM Persons WHERE year > 0 AND (year < " + minAcceptedYear + " OR year >= "
				+ maxAcceptedYear + ")";
		log.info(query);

		generalAnalysis(topic, query);
	}

	private void missingDOBAtScale() {
		String topic = "Missing DOB at scale";

		log.info(topic + " with {} at scale", missingDOBAtScale);

		int year = currentYear - 2;
		String query = "SELECT competitionId, count(distinct personId) as missingDOBs\n"
				+ "FROM Results INNER JOIN Persons ON Results.personId=Persons.id\n"
				+ "WHERE Persons.year=0 AND RIGHT(competitionId,4) > " + year + "\n"
				+ "GROUP BY competitionId HAVING missingDOBs >= " + missingDOBAtScale + " ORDER BY missingDOBs DESC";
		generalAnalysis(topic, query);

	}

	private void nameWithNumbers() {
		String topic = "Name with numbers";
		String query = "SELECT id, name FROM Persons WHERE name REGEXP '[0-9]' LIMIT 100";
		generalAnalysis(topic, query);
	}

	private void lowerCaseFirstName() {
		String topic = "Lowercase first names";
		String query = "SELECT * FROM Persons WHERE BINARY name REGEXP '^[a-z]'";
		generalAnalysis(topic, query);
	}

	private void lowerCaseLastNameWithoutLocalName() {
		String topic = "Lowercase last name without local name";
		String query = "SELECT * FROM Persons WHERE BINARY MID(REVERSE(name), LOCATE(\"\" \"\", REVERSE(name))-1,1) <> UPPER(MID(REVERSE(name), LOCATE(\"\" \"\", REVERSE(name))-1,1))";
		generalAnalysis(topic, query);
	}

	private void lowerCaseLastNameWithLocalName() {
		String topic = "Missing one bracket for local name";
		String query = "SELECT * FROM Persons WHERE name LIKE '%(%' and BINARY MID(REVERSE(LEFT(name, LOCATE('(',name)-2)), LOCATE(' ', REVERSE(LEFT(name, LOCATE('(',name)-2)))-1,1) <> UPPER(MID(REVERSE(LEFT(name, LOCATE('(',name)-2)), LOCATE(' ', REVERSE(LEFT(name, LOCATE('(',name)-2)))-1,1))";
		generalAnalysis(topic, query);
	}

	private void badLocalNames() {
		String topic = "Bad local names";
		String query = "SELECT id, name, countryId FROM Persons WHERE BINARY name REGEXP '[(].*[A-Za-z]+'";
		generalAnalysis(topic, query);
	}

	private void missingOneBracketForLocalName() {
		String topic = "Missing one bracket for local name";
		String query = "SELECT * FROM Persons WHERE (name LIKE '%(%' OR name LIKE '%)') AND name NOT LIKE '%(%)%'";
		generalAnalysis(topic, query);
	}

	private void trailingSpaces() {
		String topic = "Trailing spaces";
		String query = "SELECT * FROM Persons WHERE name LIKE '%( %' OR name LIKE '% )'";
		generalAnalysis(topic, query);
	}

	private void emptyGender() {
		String topic = "Empty gender";
		String query = "SELECT * FROM Persons WHERE gender=''";
		generalAnalysis(topic, query);
	}

	private void inconsistentWcaId() {
		String topic = "Inconsistent WCA ID";
		String query = "SELECT r.personId, r.personName, MIN(c.year) as first_year\n"
				+ "FROM Results r INNER JOIN Competitions c ON r.competitionId=c.id\n"
				+ "GROUP BY personID, r.personName HAVING first_year <> LEFT(personId,4)";
		generalAnalysis(topic, query);
	}

	private void personsTableEntriesEithoutResultsTableEntry() {
		String topic = "Persons table entries without Results table entry";
		String query = "SELECT * FROM Persons WHERE id NOT IN (SELECT personId FROM Results)";
		generalAnalysis(topic, query);
	}

}
