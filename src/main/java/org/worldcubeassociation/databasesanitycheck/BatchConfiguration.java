package org.worldcubeassociation.databasesanitycheck;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.worldcubeassociation.databasesanitycheck.tasklet.DatabaseExportDownloadTasklet;
import org.worldcubeassociation.databasesanitycheck.tasklet.ExecuteDownloadedSqlTasklet;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Autowired
	private DatabaseExportDownloadTasklet databaseExportDownloadTasklet;

	@Autowired
	private ExecuteDownloadedSqlTasklet executeDownloadedSqlTasklet;

	@Bean
	public Job downloadDatabaseExport() {
		log.info("Job to handle database export");

		// Daily DailyJobTimestamper makes this executes at most once a day
		return jobBuilderFactory.get("handleDatabaseExport").incrementer(new DailyJobTimestamper()).start(downloadExport())
				.next(executeSql()).build();
	}

	@Bean
	public Step downloadExport() {
		return stepBuilderFactory.get("downloadExport").tasklet(databaseExportDownloadTasklet).build();
	}

	@Bean
	public Step executeSql() {
		return stepBuilderFactory.get("executeSql").tasklet(executeDownloadedSqlTasklet).build();
	}
}
