package org.worldcubeassociation.dbsanitycheck.reader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.worldcubeassociation.dbsanitycheck.bean.QueryBean;
import org.worldcubeassociation.dbsanitycheck.exception.SanityCheckException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class QueryReader {

	@Value("${job.queryreader.filename}")
	private String filename;

	// We ask the user to keep categories organized together
	private String prevCategory;
	private List<String> consideredCategories = new ArrayList<>();

	public List<QueryBean> read() throws FileNotFoundException, SanityCheckException {
		log.info("Read file");

		List<QueryBean> result = new ArrayList<>();

		InputStream is = new FileInputStream(filename);
		try (Scanner scan = new Scanner(is)) {
			scan.useDelimiter("(\n){2,}");

			while (scan.hasNext()) {
				String group = scan.next().trim();
				String[] split = group.split("\n");

				if (split.length < 3) {
					throw new SanityCheckException(
							String.format("Expected group length is at least 3 lines.%n%s", group));
				}

				addQuery(result, split);
			}
		}
		log.info(String.format("Found %s categories", consideredCategories.size()));
		log.info(String.format("Found %s queries", result.size()));

		return result;
	}

	private void addQuery(List<QueryBean> result, String[] split) throws SanityCheckException {
		String category = split[0];
		String topic = split[1];
		String query = String.join("\n", Arrays.copyOfRange(split, 2, split.length));

		// If we find a new category
		if (prevCategory == null || !prevCategory.equals(category)) {

			// We make sure it is not duplicated. Categories should be organized.
			if (consideredCategories.contains(category)) {
				log.error("Categories not organized");
				consideredCategories.add(category); // Just to log a help
				throw new SanityCheckException(String
						.format("Categories should be placed together. Categories found: %s.", consideredCategories));
			}
			consideredCategories.add(category);
			prevCategory = category;
		}

		QueryBean queryBean = new QueryBean();
		queryBean.setCategory(category);
		queryBean.setTopic(topic);
		queryBean.setQuery(query);

		if (result.indexOf(queryBean) >= 0) {
			log.error("Repeated query");
			throw new SanityCheckException(String
					.format("Duplicated query (it could be same category + topic or the same sql query)%n%s", query));
		}

		result.add(queryBean);
	}
}
