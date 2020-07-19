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
import org.worldcubeassociation.databasesanitycheck.tasklet.WrtSanityCheckTasklet;

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

	@Autowired
	private WrtSanityCheckTasklet wrtSanityCheckTasklet;

	@Bean
	public Job downloadDatabaseExport() {

		// Daily DailyJobTimestamper makes this executes at most once a day

		// TODO
		// return jobBuilderFactory.get("handleDatabaseExport").incrementer(new
		// DailyJobTimestamper()).start(downloadExport())
		// .next(executeSql()).next(wrtSanityCheck()).build();

		return jobBuilderFactory.get("handleDatabaseExport").incrementer(new RunIdIncrementer())
				.start(wrtSanityCheck()).build();

	}

	@Bean
	public Step downloadExport() {
		return stepBuilderFactory.get("downloadExport").tasklet(databaseExportDownloadTasklet).build();
	}

	@Bean
	public Step executeSql() {
		return stepBuilderFactory.get("executeSql").tasklet(executeDownloadedSqlTasklet).build();
	}

	@Bean
	public Step wrtSanityCheck() {
		return stepBuilderFactory.get("wrtSanityCheck").tasklet(wrtSanityCheckTasklet).build();
	}

}
