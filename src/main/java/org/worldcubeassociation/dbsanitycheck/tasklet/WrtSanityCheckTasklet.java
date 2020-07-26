package org.worldcubeassociation.dbsanitycheck.tasklet;

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

		// Takes long in developing
		inconsistentWcaId();

		personsTableEntriesWithoutResultsTableEntry();
		resultsTableEntriesWithoutPersonsTableEntry();
		// TODO Running script 2b (""Check existing people"")

		// TODO "1. Running the ""ranks"" part of script 2a (""Check results"") for all
		// competitions

		// Irregular results
		emptyFirstSolve();
		wrongNumberOfResultsForNonCombinedRounds();
		moreThanTwoDifferentNumbersOfResultsForCombinedRounds();
		badlyAppliedCuttofs();
		onlyDnsOrZeroResults();
		nonZeroAverageForRowsWithLessThanEquals2Attempt();

		duplicateResults();

		// TODO timeLimitViolation

		// Consistency of results and scrambles
		roundInScramblesWithNoResultsButWithCompetitionIdInResults();
		roundsWithResultsButNoScramblesButWithCompetitionIdInScrambles();

		// Duplicate scrambles
		duplicatesWithinScramblesForOneCompetitionId();
		duplicatesAcrossMultipleCompetitionsIgnoring222AndSkewb();
		duplicateRows();

		// Invalid Scrambles entries
		invalidGroupIds();
		scramblesWithLeadingOrTrailingSpaces();
		// TODO Wrong number of scrambles

		// Invalid competitionIds
		lowercaseCompetitionIds();
		competitionIdsNotEndingWithYearOrEndYear();

		// Suspicious cutoffs & time limits
		timeLimitGreaterThanCutoff();
		cutoffLessThan5Seconds();
		timeLimitLessThan10Seconds();
		timeLimitGreaterThan10MinutesForFastEvents();

		// Irregular user account data
		nonExistingClaimedWCAIDs();

		// Non-Posted Results
		inconsistentNameInUsersTable();
		notPublicCompetition();

		// Show results with a warn
		for (String key : map.keySet()) {
			log.warn("Inconsistency in the topic " + key);
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

	private void duplicateResults() {
		String topic = "Duplicate results";
		String query = "SELECT value1, value2, value3, value4, value5, GROUP_CONCAT(distinct eventId) as events, GROUP_CONCAT(distinct personId) as people, GROUP_CONCAT(distinct competitionId), count(*) as amount FROM Results\n"
				+ "WHERE IF(value1>0,1,0) + IF(value2>0,1,0) + IF(value3>0,1,0) + IF(value4>0,1,0) + IF(value5>0,1,0) > 1 AND eventId not in ('333mbo', '333fm')\n"
				+ "GROUP BY value1, value2, value3, value4, value5 HAVING amount > 1 AND count(distinct competitionId) = 1 ORDER BY amount DESC LIMIT 100";
		generalAnalysis(topic, query);
	}

	// private void timeLimitViolation()

	private void roundInScramblesWithNoResultsButWithCompetitionIdInResults() {
		String topic = "Round in Scrambles with no results, but with competitionId in Results";
		String query = "SELECT distinct competitionId, eventId, roundTypeId FROM Scrambles\n"
				+ "WHERE competitionId in (SELECT competitionId FROM Results) AND CONCAT(competitionId,eventId,roundTypeId) not in (SELECT CONCAT(competitionId,eventId,roundTypeId) FROM Results) \n"
				+ "ORDER BY competitionId,eventId LIMIT 100";
		generalAnalysis(topic, query);
	}

	private void roundsWithResultsButNoScramblesButWithCompetitionIdInScrambles() {
		String topic = "Rounds with results, no scrambles, but with competitionId in Scrambles";
		String query = "SELECT distinct competitionId, eventId, roundTypeId FROM Results\n"
				+ "WHERE competitionId in (SELECT competitionId FROM Scrambles) AND CONCAT(competitionId,eventId,roundTypeId) not in (SELECT CONCAT(competitionId,eventId,roundTypeId) FROM Scrambles)\n"
				+ "ORDER BY competitionId, eventId LIMIT 100";
		generalAnalysis(topic, query);
	}

	private void duplicatesWithinScramblesForOneCompetitionId() {
		String topic = "Duplicates within scrambles for one competitionId";
		String query = "select competitionId, scramble, count(*) qt from Scrambles\n"
				+ "group by competitionId, scramble having qt > 1";
		generalAnalysis(topic, query);
	}

	private void duplicatesAcrossMultipleCompetitionsIgnoring222AndSkewb() {
		String topic = "Duplicates across multiple competitions ignoring 222 and skewb";
		String query = "SELECT GROUP_CONCAT(distinct eventId ORDER BY eventId) AS events, GROUP_CONCAT(distinct competitionId) as comps, scramble, count(scrambleId) AS scount, GROUP_CONCAT(scrambleId) AS scramleIds\n"
				+ "FROM Scrambles WHERE eventId not in ('222', 'skewb') GROUP BY scramble\n"
				+ "HAVING count(scrambleId) > 1 AND count(distinct competitionId) > 1 LIMIT 100";
		generalAnalysis(topic, query);

	}

	private void duplicateRows() {
		String topic = "Duplicate rows";
		String query = "SELECT * FROM Scrambles t1 INNER JOIN Scrambles t2\n"
				+ "WHERE t1.scrambleId < t2.scrambleId AND t1.competitionId=t2.competitionId AND t1.eventId=t2.eventId AND t1.roundTypeId=t2.roundTypeId AND t1.groupId=t2.groupId AND t1.isExtra=t2.isExtra AND t1.scrambleNum=t2.scrambleNum AND t1.scramble=t2.scramble";
		generalAnalysis(topic, query);
	}

	private void invalidGroupIds() {
		String topic = "Invalid groupIds";
		String query = "SELECT distinct groupId FROM Scrambles \n"
				+ "WHERE CAST(groupId AS BINARY) NOT REGEXP '^[A-Z]+$'";
		generalAnalysis(topic, query);
	}

	private void scramblesWithLeadingOrTrailingSpaces() {
		String topic = "Scrambles with leading or trailing spaces";
		String query = "SELECT * FROM Scrambles WHERE LENGTH(scramble) != LENGTH(TRIM(scramble))";
		generalAnalysis(topic, query);
	}

	// TODO private void wrongNumberOfScrambles()

	private void lowercaseCompetitionIds() {
		String topic = "Lowercase competitionIds";
		String query = "SELECT * FROM Competitions WHERE announced_at is not NULL and BINARY id REGEXP '^[a-z]'";
		generalAnalysis(topic, query);
	}

	private void competitionIdsNotEndingWithYearOrEndYear() {
		String topic = "competitionIds not ending with year or endYear";
		String query = "SELECT * FROM Competitions WHERE announced_at is not NULL and BINARY id REGEXP '^[a-z]'";
		generalAnalysis(topic, query);
	}

	private void timeLimitGreaterThanCutoff() {
		String topic = "Time limit > cutoff";
		String query = "SELECT ce.competition_id, ce.event_id, CONVERT(MID(ro.time_limit, 17, 10), UNSIGNED INTEGER) time_limit, CONVERT(MID(ro.cutoff, 39, 10), UNSIGNED INTEGER) cutoff\n"
				+ "FROM rounds as ro INNER JOIN competition_events as ce on ce.id = ro.competition_event_id\n"
				+ "WHERE time_limit is not NULL and ro.cutoff is not NULL\n"
				+ "HAVING time_limit> 0 and time_limit < cutoff";
		generalAnalysis(topic, query);
	}

	private void cutoffLessThan5Seconds() {
		String topic = "Cutoff < 5 seconds";
		String query = "SELECT ce.competition_id, ce.event_id, CONVERT(MID(ro.cutoff, 39, 10), UNSIGNED INTEGER) cutoff\n"
				+ "FROM rounds as ro INNER JOIN competition_events as ce on ce.id = ro.competition_event_id\n"
				+ "WHERE ro.cutoff is not NULL and ce.event_id <> '333fm' \n" + "HAVING cutoff < 500";
		generalAnalysis(topic, query);
	}

	private void timeLimitLessThan10Seconds() {
		String topic = "Time limit < 10 seconds";
		String query = "SELECT ce.competition_id, ce.event_id, CONVERT(MID(ro.time_limit, 17, 10), UNSIGNED INTEGER) time_limit\n"
				+ "FROM rounds as ro INNER JOIN competition_events as ce on ce.id = ro.competition_event_id\n"
				+ "WHERE time_limit is not NULL\n" + "HAVING time_limit> 0 and time_limit < 1000";
		generalAnalysis(topic, query);
	}

	private void timeLimitGreaterThan10MinutesForFastEvents() {
		String topic = "Time limit > 10 minutes for fast events";
		String query = "SELECT ce.competition_id, ce.event_id, CONVERT(MID(ro.time_limit, 17, 10), UNSIGNED INTEGER) time_limit\n"
				+ "FROM rounds as ro INNER JOIN competition_events as ce on ce.id = ro.competition_event_id\n"
				+ "WHERE time_limit is not NULL and ce.event_id in ('333', '222', '444', '333oh', 'clock', 'mega', 'pyram', 'skewb', 'sq1') HAVING time_limit> 60000";
		generalAnalysis(topic, query);
	}

	private void nonExistingClaimedWCAIDs() {
		String topic = "Non-existing claimed WCA-IDs";
		String query = "SELECT * FROM users where wca_id not in (SELECT id FROM Persons)";
		generalAnalysis(topic, query);
	}

	private void inconsistentNameInUsersTable() {
		String topic = "Inconsistent name in users table";
		String query = "SELECT p.id, p.name as profile_name, u.name as account_name FROM Persons p \n"
				+ "INNER JOIN users u ON p.id=u.wca_id AND p.name<>u.name AND p.subId=1";
		generalAnalysis(topic, query);
	}

	private void notPublicCompetition() {
		String topic = "Find not public competition";
		String query = "SELECT id FROM Competitions WHERE results_posted_by IS NULL AND announced_at IS NOT NULL AND id IN (SELECT competitionId FROM Results)";
		generalAnalysis(topic, query);
	}

}
