package org.worldcubeassociation.databasesanitycheck.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.worldcubeassociation.databasesanitycheck.api.WcaApi;

@Component
public class DatabaseExportDownloadTasklet implements Tasklet {

	@Autowired
	private WcaApi wcaApi;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		wcaApi.getDatabaseExport();
		return RepeatStatus.FINISHED;
	}

}
