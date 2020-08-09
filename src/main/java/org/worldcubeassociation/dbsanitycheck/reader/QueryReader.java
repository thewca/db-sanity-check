package org.worldcubeassociation.dbsanitycheck.reader;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.worldcubeassociation.dbsanitycheck.bean.QueryBean;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class QueryReader {

	public List<QueryBean> read() throws Exception {
		ClassPathResource queriesFile = new ClassPathResource("queries.csv");
		
		log.info("Read from file {}", queriesFile.getPath());
		
		FlatFileItemReader<QueryBean> itemReader = new FlatFileItemReader<>();
		itemReader.setResource(queriesFile);
		
		// DelimitedLineTokenizer defaults to comma as its delimiter
		DefaultLineMapper<QueryBean> lineMapper = new DefaultLineMapper<>();
		lineMapper.setLineTokenizer(new DelimitedLineTokenizer());
		lineMapper.setFieldSetMapper(new QueryBeanFieldSetMapper());
		itemReader.setLineMapper(lineMapper);
		itemReader.open(new ExecutionContext());

		List<QueryBean> queries = new ArrayList<>();
		while (true) {
			QueryBean query = itemReader.read();
			if (query == null) {
				break;
			}

			queries.add(query);

		}

		return queries;

	}

	protected static class QueryBeanFieldSetMapper implements FieldSetMapper<QueryBean> {
		public QueryBean mapFieldSet(FieldSet fieldSet) {
			QueryBean query = new QueryBean();

			query.setCategory(fieldSet.readString(0));
			query.setTopic(fieldSet.readString(1));
			query.setQuery(fieldSet.readString(2));

			return query;
		}
	}

}
