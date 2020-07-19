package org.worldcubeassociation.databasesanitycheck;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
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

		// TODO set step1 back again
//		return jobBuilderFactory.get("handleDatabaseExport").incrementer(new RunIdIncrementer()).start(step1())
//				.next(step2()).build();

		return jobBuilderFactory.get("handleDatabaseExport").incrementer(new RunIdIncrementer()).start(step2()).build();

	}

	@Bean
	public Step step1() {
		return stepBuilderFactory.get("downloadExport").tasklet(databaseExportDownloadTasklet).build();
	}

	@Bean
	public Step step2() {
		return stepBuilderFactory.get("executeSql").tasklet(executeDownloadedSqlTasklet).build();
	}
}
