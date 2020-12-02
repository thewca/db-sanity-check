package org.worldcubeassociation.dbsanitycheck.bean;

import lombok.Data;

@Data
public class QueryWithErrorBean {
	private QueryBean queryBean;
	private String error;
}
