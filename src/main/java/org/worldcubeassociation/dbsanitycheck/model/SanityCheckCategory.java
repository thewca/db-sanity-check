package org.worldcubeassociation.dbsanitycheck.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "sanity_check_categories")
public class SanityCheckCategory {
	@Id
	private Long id;

	private String name;
}
