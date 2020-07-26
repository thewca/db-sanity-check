package org.worldcubeassociation.dbsanitycheck;

import java.time.LocalDate;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;

public class DailyJobTimestamper implements JobParametersIncrementer {
	
	@Override
	public JobParameters getNext(JobParameters parameters) {
		return new JobParametersBuilder(parameters).addString("currentDate", LocalDate.now().toString())
				.toJobParameters();
	}
}