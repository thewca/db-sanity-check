package org.worldcubeassociation.dbsanitycheck.api;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WcaApi {

	@Value("${api.wca.baseUrl}")
	private String baseUrl;

	@Value("${api.wca.export.path}")
	private String exportPath;

	@Value("${wca.export.local.path}")
	private String localExportPath;

	@Value("${wca.export.local.filename}")
	private String localExportFilename;

	public void getDatabaseExport() throws IOException, URISyntaxException {
		URL url = new URL(baseUrl + exportPath);
		log.info("url: " + url.toURI());

		File file = new File(localExportPath + localExportFilename);
		
		// Saving the file in an -api- file is not pretty, but it's ok for now.
		FileUtils.copyURLToFile(url, file);
		log.info("Saved to " + file.getAbsolutePath());
	}

}