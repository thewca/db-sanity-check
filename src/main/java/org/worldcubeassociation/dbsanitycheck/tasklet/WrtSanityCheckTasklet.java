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

	private Map<String, String> queries = new HashMap<>();
	private Map<String, List<String>> map = new HashMap<>();

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
			throws DataAccessException, IOException, SQLException {

		fillQueries();
		log.info("{} queries found", queries.size());

		executeQueries();
		showResults();

		return RepeatStatus.FINISHED;
	}

	private void fillQueries() {
		// This method is intended to be easily maintainable also by people with no java
		// skills
		String topic;
		String query;

		// Begin canonical example
		topic = "Name with numbers"; // A clear topic
		query = "SELECT id, name FROM Persons WHERE name REGEXP '[0-9]' LIMIT 100"; // The double checked SQL query
		queries.put(topic, query); // Add it with the others
		// End canonical example

		topic = "Lowercase first names";
		query = "SELECT * FROM Persons WHERE BINARY name REGEXP '^[a-z]'";
		queries.put(topic, query);

		topic = "Lowercase last name without local name";
		query = "SELECT * FROM Persons WHERE BINARY MID(REVERSE(name), LOCATE(\"\" \"\", REVERSE(name))-1,1) <> UPPER(MID(REVERSE(name), LOCATE(\"\" \"\", REVERSE(name))-1,1))";
		queries.put(topic, query);

		topic = "Lowercase last name with local name";
		query = "SELECT * FROM Persons WHERE name LIKE '%(%' and BINARY MID(REVERSE(LEFT(name, LOCATE('(',name)-2)), LOCATE(' ', REVERSE(LEFT(name, LOCATE('(',name)-2)))-1,1) <> UPPER(MID(REVERSE(LEFT(name, LOCATE('(',name)-2)), LOCATE(' ', REVERSE(LEFT(name, LOCATE('(',name)-2)))-1,1))";
		queries.put(topic, query);

		topic = "Bad local names";
		query = "SELECT id, name, countryId FROM Persons WHERE BINARY name REGEXP '[(].*[A-Za-z]+'";
		queries.put(topic, query);

		topic = "Missing one bracket for local name";
		query = "SELECT * FROM Persons WHERE (name LIKE '%(%' OR name LIKE '%)') AND name NOT LIKE '%(%)%'";
		queries.put(topic, query);

		topic = "Trailing whitespaces in names";
		query = "SELECT * FROM Persons WHERE name LIKE ' %' OR name LIKE '% '";
		queries.put(topic, query);

		topic = "Trailing whitespaces in local names";
		query = "SELECT * FROM Persons WHERE name LIKE '%( %' OR name LIKE '% )'";
		queries.put(topic, query);

		topic = "Incorrect DOB";
		int minAcceptedYear = currentYear - maxAcceptedAge;
		int maxAcceptedYear = currentYear - minAcceptedAge;
		query = "SELECT * FROM Persons WHERE year > 0 AND (year < " + minAcceptedYear + " OR year >= " + maxAcceptedYear
				+ ")";
		queries.put(topic, query);

		topic = "Missing DOB at scale";
		int year = currentYear - 2;
		query = "SELECT competitionId, count(distinct personId) as missingDOBs\n"
				+ "FROM Results INNER JOIN Persons ON Results.personId=Persons.id\n"
				+ "WHERE Persons.year=0 AND RIGHT(competitionId,4) > " + year + "\n"
				+ "GROUP BY competitionId HAVING missingDOBs >= " + missingDOBAtScale + " ORDER BY missingDOBs DESC";
		queries.put(topic, query);

		topic = "Empty gender";
		query = "SELECT * FROM Persons WHERE gender=''";
		queries.put(topic, query);

		topic = "Inconsistent WCA ID";
		query = "SELECT r.personId, MIN(c.year) as first_year\n"
				+ "FROM Results r INNER JOIN Competitions c ON r.competitionId=c.id\n"
				+ "GROUP BY personID HAVING first_year <> LEFT(personId,4)";
		queries.put(topic, query);

		topic = "Persons table entries without Results table entry";
		query = "SELECT * FROM Persons WHERE id NOT IN (SELECT personId FROM Results)";
		queries.put(topic, query);

		topic = "Results table entries without Persons table entry";
		query = "SELECT * FROM Results WHERE personId NOT IN (SELECT id FROM Persons)";
		queries.put(topic, query);

		// TODO
		// 3. Running script 2b (""Check existing people""):
		// https://www.worldcubeassociation.org/results/admin/persons_check_finished.php"

		topic = "Empty first solve";
		query = "SELECT * FROM Results WHERE value1=0";
		queries.put(topic, query);

		topic = "Wrong number of results for non-combined rounds";
		query = "SELECT * FROM Results as r INNER JOIN Formats as f ON r.formatId = f.id \n"
				+ "WHERE r.roundTypeId in (SELECT id FROM RoundTypes WHERE not name LIKE 'Combined%') \n"
				+ "AND f.expected_solve_count <> IF(value1<>0,1,0) + IF(value2<>0,1,0) + IF(value3<>0,1,0) + IF(value4<>0,1,0) + IF(value5<>0,1,0)";
		queries.put(topic, query);

		// 2013 or later
		topic = "More than two different numbers of results for combined rounds (2013 or later)";
		query = "SELECT competitionId, eventId, roundTypeId, COUNT(solves) as num_results\n"
				+ "FROM (SELECT DISTINCT competitionId, eventId, roundTypeId, IF(value1<>0,1,0) \n"
				+ " + IF(value2<>0,1,0) + IF(value3<>0,1,0) + IF(value4<>0,1,0) + IF(value5<>0,1,0) as solves\n"
				+ "FROM Results WHERE RIGHT(competitionId, 4) >= 2013) re\n"
				+ "GROUP BY competitionId, eventId, roundTypeId \n"
				+ "HAVING IF(roundTypeId in (\"c\",\"d\",\"e\",\"g\",\"h\"), num_results > 2, num_results > 1)\n"
				+ "ORDER BY competitionId LIMIT 100";
		queries.put(topic, query);

		topic = "Badly applied cutoffs";
		query = "SELECT a.competitionId, a.eventId, a.roundTypeId, b.cutoff, GROUP_CONCAT(a.personId) as violations FROM Results as a\n"
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
		queries.put(topic, query);

		topic = "Only DNS or zero results";
		query = "SELECT * FROM Results WHERE ABS(1+value1)=1 AND ABS(1+value2)=1 AND ABS(1+value3)=1 AND ABS(1+value4)=1 AND ABS(1+value5)=1";
		queries.put(topic, query);

		topic = "Non-zero average for rows with<=2 attempt";
		query = "SELECT * FROM Results WHERE average <> 0 AND IF(value1<>0,1,0) + IF(value2<>0,1,0) + IF(value3<>0,1,0) + IF(value4<>0,1,0) + IF(value5<>0,1,0) <= 2";
		queries.put(topic, query);

		topic = "Duplicate results";
		query = "SELECT value1, value2, value3, value4, value5, GROUP_CONCAT(distinct eventId) as events, GROUP_CONCAT(distinct personId) as people, GROUP_CONCAT(distinct competitionId), count(*) as amount FROM Results\n"
				+ "WHERE IF(value1>0,1,0) + IF(value2>0,1,0) + IF(value3>0,1,0) + IF(value4>0,1,0) + IF(value5>0,1,0) > 1 AND eventId not in ('333mbo', '333fm')\n"
				+ "GROUP BY value1, value2, value3, value4, value5 HAVING amount > 1 AND count(distinct competitionId) = 1 ORDER BY amount DESC LIMIT 100";
		queries.put(topic, query);

		// private void timeLimitViolation()

		topic = "Round in Scrambles with no results, but with competitionId in Results";
		query = "SELECT distinct competitionId, eventId, roundTypeId FROM Scrambles\n"
				+ "WHERE competitionId in (SELECT competitionId FROM Results) AND CONCAT(competitionId,eventId,roundTypeId) not in (SELECT CONCAT(competitionId,eventId,roundTypeId) FROM Results) \n"
				+ "ORDER BY competitionId,eventId LIMIT 100";
		queries.put(topic, query);

		topic = "Rounds with results, no scrambles, but with competitionId in Scrambles";
		query = "SELECT distinct competitionId, eventId, roundTypeId FROM Results\n"
				+ "WHERE competitionId in (SELECT competitionId FROM Scrambles) AND CONCAT(competitionId,eventId,roundTypeId) not in (SELECT CONCAT(competitionId,eventId,roundTypeId) FROM Scrambles)\n"
				+ "ORDER BY competitionId, eventId LIMIT 100";
		queries.put(topic, query);

		// TODO Duplicates within scrambles for one competitionId"

		topic = "Duplicates across multiple competitions ignoring 222 and skewb";
		query = "SELECT GROUP_CONCAT(distinct eventId ORDER BY eventId) AS events, GROUP_CONCAT(distinct competitionId) as comps, \n"
				+ "	scramble, count(scrambleId) AS scount, GROUP_CONCAT(scrambleId) AS scramleIds\n"
				+ "FROM (SELECT dups.scramble, scrambleId, competitionId, eventId\n"
				+ "        FROM (SELECT scramble FROM Scrambles \n"
				+ "              WHERE eventId not in ('222', 'skewb') \n"
				+ "              GROUP BY scramble HAVING count(*) > 1) dups\n" + "        INNER JOIN Scrambles\n"
				+ "		ON dups.scramble = Scrambles.scramble) t\n"
				+ "GROUP BY scramble HAVING count(distinct competitionId) > 1";
		queries.put(topic, query);

		topic = "Duplicate rows (multiple imports)";
		query = "SELECT * FROM Scrambles t1 INNER JOIN Scrambles t2\n"
				+ "WHERE t1.scrambleId < t2.scrambleId AND t1.competitionId=t2.competitionId AND t1.eventId=t2.eventId AND t1.roundTypeId=t2.roundTypeId AND t1.groupId=t2.groupId AND t1.isExtra=t2.isExtra AND t1.scrambleNum=t2.scrambleNum AND t1.scramble=t2.scramble";
		queries.put(topic, query);

		topic = "Invalid groupIds";
		query = "SELECT distinct groupId FROM Scrambles WHERE CAST(groupId AS BINARY) NOT REGEXP '^[A-Z]+$'";
		queries.put(topic, query);

		topic = "Scrambles with leading or trailing spaces";
		query = "SELECT * FROM Scrambles WHERE LENGTH(scramble) != LENGTH(TRIM(scramble))";
		queries.put(topic, query);

		// TODO wrongNumberOfScrambles()

		topic = "Lowercase competitionIds";
		query = "SELECT * FROM Competitions WHERE announced_at is not NULL and BINARY id REGEXP '^[a-z]'";
		queries.put(topic, query);

		topic = "competitionIds not ending with year or endYear";
		query = "SELECT * FROM Competitions WHERE announced_at is not NULL AND RIGHT(id,4) <> year AND RIGHT(id,4) <> endYear";
		queries.put(topic, query);

		topic = "Time limit > cutoff";
		query = "SELECT ce.competition_id, ce.event_id, CONVERT(MID(ro.time_limit, 17, 10), UNSIGNED INTEGER) time_limit, CONVERT(MID(ro.cutoff, 39, 10), UNSIGNED INTEGER) cutoff\n"
				+ "FROM rounds as ro INNER JOIN competition_events as ce on ce.id = ro.competition_event_id\n"
				+ "WHERE time_limit is not NULL and ro.cutoff is not NULL\n"
				+ "HAVING time_limit> 0 and time_limit < cutoff";
		queries.put(topic, query);

		topic = "Cutoff < 5 seconds";
		query = "SELECT ce.competition_id, ce.event_id, CONVERT(MID(ro.cutoff, 39, 10), UNSIGNED INTEGER) cutoff\n"
				+ "FROM rounds as ro INNER JOIN competition_events as ce on ce.id = ro.competition_event_id\n"
				+ "WHERE ro.cutoff is not NULL and ce.event_id <> '333fm' \n" + "HAVING cutoff < 500";
		queries.put(topic, query);

		topic = "Time limit < 10 seconds";
		query = "SELECT ce.competition_id, ce.event_id, CONVERT(MID(ro.time_limit, 17, 10), UNSIGNED INTEGER) time_limit\n"
				+ "FROM rounds as ro INNER JOIN competition_events as ce on ce.id = ro.competition_event_id\n"
				+ "WHERE time_limit is not NULL HAVING time_limit> 0 and time_limit < 1000";
		queries.put(topic, query);

		topic = "Time limit > 10 minutes for fast events";
		query = "SELECT ce.competition_id, ce.event_id, CONVERT(MID(ro.time_limit, 17, 10), UNSIGNED INTEGER) time_limit\n"
				+ "FROM rounds as ro INNER JOIN competition_events as ce on ce.id = ro.competition_event_id\n"
				+ "WHERE time_limit is not NULL and ce.event_id in ('333', '222', '444', '333oh', 'clock', 'mega', 'pyram', 'skewb', 'sq1') HAVING time_limit> 60000";
		queries.put(topic, query);

		topic = "Non-existing claimed WCA-IDs";
		query = "SELECT * FROM users where wca_id not in (SELECT id FROM Persons)";
		queries.put(topic, query);

		topic = "Inconsistent name in users table";
		query = "SELECT p.id, p.name, u.name FROM Persons p \n" + 
				"INNER JOIN users u ON p.id=u.wca_id AND p.name<>u.name AND p.subId=1";
		queries.put(topic, query);

		// TODO Big BLD Means

		topic = "Find not public competition";
		query = "SELECT id FROM Competitions WHERE results_posted_by IS NULL AND announced_at IS NOT NULL AND id IN (SELECT competitionId FROM Results)";
		queries.put(topic, query);
	}

	private void executeQueries() {
		queries.keySet().forEach(topic -> {
			String query = queries.get(topic);
			generalAnalysis(topic, query);
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
