package org.worldcubeassociation.databasesanitycheck.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.worldcubeassociation.databasesanitycheck.api.WcaApi;

import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

// Not used for now. get_db_export.sh replace this.

@Slf4j
@Component
public class DatabaseExportDownloadTasklet implements Tasklet {

	@Autowired
	private WcaApi wcaApi;
	
	@Value("${wca.export.local.path}")
	private String localExportPath;

	@Value("${wca.export.local.filename}")
	private String localExportFilename;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		
		// This also saves the file to local
		wcaApi.getDatabaseExport();
		
		unzip();
		return RepeatStatus.FINISHED;
	}
	
	private void unzip() {

		String localExport = localExportPath + localExportFilename;
		log.info("Unzip " + localExport + " to the folder " + localExportPath);
		try {
			ZipFile zipFile = new ZipFile(localExport);
			zipFile.extractAll(localExportPath);
		} catch (ZipException e) {
			e.printStackTrace();
		}
	}

}
