package org.worldcubeassociation.dbsanitycheck.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseBean {
	private String category;
	private String topic;
}
