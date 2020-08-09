package org.worldcubeassociation.dbsanitycheck.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.worldcubeassociation.dbsanitycheck.bean.CategoryBean;
import org.worldcubeassociation.dbsanitycheck.bean.QueryBean;
import org.worldcubeassociation.dbsanitycheck.exception.SanityCheckException;
import org.worldcubeassociation.dbsanitycheck.reader.QueryReader;

import lombok.Getter;

@Component
public class QueryHelper {
	@Autowired
	private QueryReader queryReader;

	@Getter
	private Map<String, CategoryBean> categories = new HashMap<>();
	private int numberOfQueries = 0;
	
	public void read() throws UnexpectedInputException, ParseException, Exception {
		List<QueryBean> queries = queryReader.read();
		for (QueryBean query : queries) {
			numberOfQueries++;
			add(query);
		}
	}
	
	private void add(QueryBean queryBean) throws SanityCheckException {
		String category = queryBean.getCategory();
		String topic = queryBean.getTopic();
		String query = queryBean.getQuery();
		
		validateParam("Category", category);
		validateParam("Topic", topic);
		validateParam("Query", query);

		// Gets from or create the key in the hashMap
		CategoryBean cat = categories.computeIfAbsent(category, s -> new CategoryBean(category));
		cat.addQuery(queryBean);
	}

	public CategoryBean getQueriesByCategory(String category) {
		return categories.get(category);
	}

	public int size() {
		return numberOfQueries;
	}

	private void validateParam(String name, String value) throws SanityCheckException {
		if (value == null) {
			throw new SanityCheckException(String.format("%s can not be bull", name));
		}
	}

}
