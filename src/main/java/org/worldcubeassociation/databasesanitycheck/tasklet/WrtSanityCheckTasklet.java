package org.worldcubeassociation.databasesanitycheck.tasklet;

import java.io.IOException;
import java.sql.SQLException;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WrtSanityCheckTasklet implements Tasklet {

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
			throws DataAccessException, IOException, SQLException {

		jdbcTemplate.query(
		        "select * from Formats;",
		        (rs, rowNum) -> ""+rs.getString("id")+" "+ rs.getString("name")
		    ).forEach(format -> log.info(format.toString()));

		return RepeatStatus.FINISHED;
	}

}
