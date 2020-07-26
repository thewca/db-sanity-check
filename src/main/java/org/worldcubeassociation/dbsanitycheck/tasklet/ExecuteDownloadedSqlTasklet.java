package org.worldcubeassociation.dbsanitycheck.tasklet;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

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

//Not used for now. get_db_export.sh replace this.

@Slf4j
@Component
public class ExecuteDownloadedSqlTasklet implements Tasklet {

	@Value("${wca.export.local.path}")
	private String localExportPath;

	@Value("${wca.export.local.filename.extracted}")
	private String localExportExtractedFilename;

	@Autowired
	JdbcTemplate jdbcTemplate;

	private Scanner s;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
			throws DataAccessException, IOException, SQLException {
		log.info("Execute downloaded SQL");

		String filename = localExportPath + localExportExtractedFilename;
		File file = new File(filename);
		
		s = new Scanner(file).useDelimiter("\n\n");

		while (s.hasNext()) {
			String query = s.next();
			jdbcTemplate.execute(query);
		}

		// TODO. This reads the entire file extracted and execute it. It may cause
		// OutOfMemoryError.
		// Perhaps there's a better way or we could brake in statements.
		// ScriptUtils.executeSqlScript(jdbcTemplate.getDataSource().getConnection(),
		// new FileSystemResource(file));

		return RepeatStatus.FINISHED;
	}

}
