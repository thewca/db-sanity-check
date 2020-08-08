package org.worldcubeassociation.dbsanitycheck.helper;

import java.util.HashMap;
import java.util.Map;

import org.worldcubeassociation.dbsanitycheck.bean.CategoryBean;
import org.worldcubeassociation.dbsanitycheck.exception.SanityCheckException;

import lombok.Data;

@Data
public class QueryHelper {
	private Map<String, CategoryBean> categories = new HashMap<>();

	public void add(String category, String topic, String query) throws SanityCheckException {
		validateParam("Category", category);
		validateParam("Topic", topic);
		validateParam("Query", query);

		CategoryBean cat = categories.getOrDefault(category, new CategoryBean(topic));
		cat.addQuery(topic, query);
	}

	public CategoryBean getQueriesByCategory(String category) {
		return categories.get(category);
	}

	public int size() {
		int result = 0;
		for (String cat : categories.keySet()) {
			result += categories.get(cat).size();
		}
		return result;
	}

	private void validateParam(String name, String value) throws SanityCheckException {
		if (value == null) {
			throw new SanityCheckException(String.format("%s can not be bull", name));
		}
	}

}
