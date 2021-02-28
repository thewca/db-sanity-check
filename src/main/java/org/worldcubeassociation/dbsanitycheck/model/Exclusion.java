package org.worldcubeassociation.dbsanitycheck.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "sanity_check_exclusions")
public class Exclusion {
	@Id
	private Integer id;
	
	@Column(name = "sanity_check_id")
	private Integer sanityCheckId;
	
	// This is actually a json and we could use hibernate types
	// TODO https://github.com/vladmihalcea/hibernate-types
	private String exclusion;
	
	private String comments;
}
