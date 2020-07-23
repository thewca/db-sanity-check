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

		/*incorrectDOB();

		missingDOBAtScale();

		nameWithNumbers();
		lowerCaseFirstName();
		lowerCaseLastNameWithoutLocalName();
		lowerCaseLastNameWithLocalName();

		badLocalNames();
		missingOneBracketForLocalName();
		trailingSpaces();

		emptyGender();

		// TODO takes long in developing
		// inconsistentWcaId();

		personsTableEntriesWithoutResultsTableEntry();
		resultsTableEntriesWithoutPersonsTableEntry();
		// TODO Running script 2b (""Check existing people"")

		// TODO "1. Running the ""ranks"" part of script 2a (""Check results"") for all
		// competitions*/

		// Irregular results
		emptyFirstSolve();
		wrongNumberOfResultsForNonCombinedRounds();
		moreThanTwoDifferentNumbersOfResultsForCombinedRounds();
		badlyAppliedCuttofs();
		onlyDnsOrZeroResults();
		nonZeroAverageForRowsWithLessThanEquals2Attempt();

		// Show results with a warn
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

	private void personsTableEntriesWithoutResultsTableEntry() {
		String topic = "Persons table entries without Results table entry";
		String query = "SELECT * FROM Persons WHERE id NOT IN (SELECT personId FROM Results)";
		generalAnalysis(topic, query);
	}

	private void resultsTableEntriesWithoutPersonsTableEntry() {
		String topic = "Results table entries without Persons table entry";
		String query = "SELECT * FROM Results WHERE personId NOT IN (SELECT id FROM Persons)";
		generalAnalysis(topic, query);
	}

	private void emptyFirstSolve() {
		String topic = "Empty first solve";
		String query = "SELECT * FROM Results WHERE value1=0";
		generalAnalysis(topic, query);
	}

	private void wrongNumberOfResultsForNonCombinedRounds() {
		String topic = "Wrong number of results for non-combined rounds";
		String query = "SELECT * FROM Results as r INNER JOIN Formats as f ON r.formatId = f.id \n"
				+ "WHERE r.roundTypeId in (SELECT id FROM RoundTypes WHERE not name LIKE 'Combined%') \n"
				+ "AND f.expected_solve_count <> IF(value1<>0,1,0) + IF(value2<>0,1,0) + IF(value3<>0,1,0) + IF(value4<>0,1,0) + IF(value5<>0,1,0)";
		generalAnalysis(topic, query);
	}

	// 2013 or later
	private void moreThanTwoDifferentNumbersOfResultsForCombinedRounds() {
		String topic = "More than two different numbers of results for combined rounds (2013 or later)";
		String query = "SELECT RIGHT(competitionId,4) as year, competitionId, eventId, roundTypeId, \n"
				+ "COUNT(distinct IF(value1<>0,1,0) + IF(value2<>0,1,0) + IF(value3<>0,1,0) + IF(value4<>0,1,0) + IF(value5<>0,1,0)) as num_results\n"
				+ "FROM Results WHERE roundTypeId in ('c','d','e','g','h') and RIGHT(competitionId,4) >= 2013\n"
				+ "GROUP BY competitionId, eventId, roundTypeId HAVING num_results > 2\n"
				+ "ORDER BY year DESC, competitionId LIMIT 100";
		generalAnalysis(topic, query);
	}

	private void badlyAppliedCuttofs() {
		String topic = "Badly applied cutoffs";
		String query = "SELECT a.competitionId, a.eventId, a.roundTypeId, b.cutoff, GROUP_CONCAT(a.personId) as violations FROM Results as a\n"
				+ "INNER JOIN (SELECT competitionId, eventId, roundTypeId, MIN(best) as cutoff, MAX(ABS(value2)) as ref2, MAX(ABS(value3)) as ref3 FROM Results\n"
				+ "     WHERE roundTypeId in ('c','d','e','g','h') and formatId in ('a','m') and average=0 and best>0\n"
				+ "     GROUP BY competitionId, eventId, roundTypeId) as b\n"
				+ "ON a.competitionId=b.competitionId AND a.eventId=b.eventId AND a.roundTypeId=b.roundTypeId\n"
				+ "WHERE a.roundTypeId in ('c','d','e','g','h') AND average<>0 AND \n"
				+ "((ref2=0 AND (value1<0 OR value1>cutoff)) \n"
				+ " OR (ref3=0 AND (value1<0 OR value1>cutoff) AND (value2<0 OR value2>cutoff))\n"
				+ " OR (value1<0 OR value1>cutoff) AND (value2<0 OR value2>cutoff) AND (value3<0 OR value3>cutoff))\n"
				+ "GROUP BY competitionId, eventId, roundTypeId\n"
				+ "ORDER BY RIGHT(a.competitionId,4), competitionId LIMIT 100";
		generalAnalysis(topic, query);
	}

	private void onlyDnsOrZeroResults() {
		String topic = "Only DNS or zero results";
		String query = "SELECT * FROM Results WHERE ABS(1+value1)=1 AND ABS(1+value2)=1 AND ABS(1+value3)=1 AND ABS(1+value4)=1 AND ABS(1+value5)=1";
		generalAnalysis(topic, query);
	}

	private void nonZeroAverageForRowsWithLessThanEquals2Attempt() {
		String topic = "Non-zero average for rows with<=2 attempt";
		String query = "SELECT * FROM Results WHERE average <> 0 AND IF(value1<>0,1,0) + IF(value2<>0,1,0) + IF(value3<>0,1,0) + IF(value4<>0,1,0) + IF(value5<>0,1,0) <= 2";
		generalAnalysis(topic, query);
	}

}
