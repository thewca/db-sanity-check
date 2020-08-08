package org.worldcubeassociation.dbsanitycheck.bean;

import java.util.ArrayList;
import java.util.List;

import org.worldcubeassociation.dbsanitycheck.exception.SanityCheckException;

import lombok.Data;

@Data
public class CategoryBean {
	private String category;
	private List<QueryBean> queries = new ArrayList<>();

	public CategoryBean(String category) {
		this.category = category;
	}

	public void addQuery(String topic, String query) throws SanityCheckException {
		QueryBean queryBean = new QueryBean();
		queryBean.setTopic(topic);
		queryBean.setQuery(query);

		if (queries.contains(queryBean)) {
			throw new SanityCheckException(String.format("Topic %s already exists.", topic));
		}

	}

	public int size() {
		return queries.size();
	}
}
