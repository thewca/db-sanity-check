package org.worldcubeassociation.dbsanitycheck.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class QueryBean {
	private String topic;

	// We do not consider the query value for equality
	// This helps asserting a query does not exists before inserting
	@EqualsAndHashCode.Exclude
	private String query;
}
