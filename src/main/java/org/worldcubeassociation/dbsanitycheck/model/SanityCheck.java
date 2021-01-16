package org.worldcubeassociation.dbsanitycheck.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "sanity_checks")
public class SanityCheck {
	@Id
	private Long id;
	
	private String topic;
	
	private String comments;
	
	private String query;
}
