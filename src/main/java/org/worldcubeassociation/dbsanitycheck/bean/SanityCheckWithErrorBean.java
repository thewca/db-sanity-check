package org.worldcubeassociation.dbsanitycheck.bean;

import org.worldcubeassociation.dbsanitycheck.model.SanityCheck;

import lombok.Data;

@Data
public class SanityCheckWithErrorBean {
	private SanityCheck sanityCheck;
	private String error;
}
