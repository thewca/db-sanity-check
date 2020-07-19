package org.worldcubeassociation.databasesanitycheck.tasklet;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ExecuteDownloadedSqlTasklet implements Tasklet {

	@Value("${wca.export.local.path}")
	private String localExportPath;

	@Value("${wca.export.local.filename.extracted}")
	private String localExportExtractedFilename;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
			throws DataAccessException, IOException, SQLException {
		log.info("Execute downloaded SQL");

		String filename = localExportPath + localExportExtractedFilename;
		File file = new File(filename);

		// TODO. This reads the entire file extracted and execute it. It may cause OutOfMemoryError.
		// Perhaps there's a better way or we could brake in statements.
		ScriptUtils.executeSqlScript(jdbcTemplate.getDataSource().getConnection(), new FileSystemResource(file));

		return RepeatStatus.FINISHED;
	}

}
