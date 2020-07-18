package org.worldcubeassociation.databasesanitycheck.api;

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

	@Value("${api.wca.export.filename}")
	private String filename;

	public void getDatabaseExport() throws IOException, URISyntaxException {
		log.info("Get database export");

		URL url = new URL(baseUrl + exportPath);
		log.info("url: " + url.toURI());

		File file = new File(filename);
		FileUtils.copyURLToFile(url, file);
		log.info("Saved to " + file.getAbsolutePath());
	}

}