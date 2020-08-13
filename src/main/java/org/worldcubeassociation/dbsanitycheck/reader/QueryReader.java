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

				String category = split[0];
				String topic = split[1];
				String query = String.join("\n", Arrays.copyOfRange(split, 2, split.length));

				QueryBean queryBean = new QueryBean();
				queryBean.setCategory(category);
				queryBean.setTopic(topic);
				queryBean.setQuery(query);

				result.add(queryBean);
			}
		}
		log.info("Found {} queries", result.size());

		return result;
	}
}
