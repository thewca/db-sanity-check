package org.worldcubeassociation.dbsanitycheck;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.worldcubeassociation.dbsanitycheck.tasklet.WrtSanityCheckTasklet;

//This is not yet in use, until the invalid date gets fixed on the export.
//https://github.com/thewca/worldcubeassociation.org/blob/44161b981c891032fecc3c4ed8944521d87dff3b/WcaOnRails/db/structure.sql#L99

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Autowired
	private WrtSanityCheckTasklet wrtSanityCheckTasklet;

	@Bean
	public Job downloadDatabaseExport() {

		return jobBuilderFactory.get("handleDatabaseExport").start(wrtSanityCheck()).incrementer(new RunIdIncrementer())
				.build();
	}

	@Bean
	public Step wrtSanityCheck() {
		return stepBuilderFactory.get("wrtSanityCheck").tasklet(wrtSanityCheckTasklet).build();
	}

}
