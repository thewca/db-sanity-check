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

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Autowired
	private WrtSanityCheckTasklet wrtSanityCheckTasklet;

	@Bean
	public Job wrtSanityCheck() {
		return jobBuilderFactory.get("wrtSanityCheck").start(sanityCheck()).incrementer(new RunIdIncrementer()).build();
	}

	@Bean
	public Step sanityCheck() {
		return stepBuilderFactory.get("wrtSanityCheck").tasklet(wrtSanityCheckTasklet).build();
	}

}
