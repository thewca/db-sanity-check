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

	public void addQuery(QueryBean queryBean) throws SanityCheckException {
		if (queries.contains(queryBean)) {
			throw new SanityCheckException(String.format("Topic %s already exists.", queryBean));
		}

		queries.add(queryBean);
	}

	public int size() {
		return queries.size();
	}
}
