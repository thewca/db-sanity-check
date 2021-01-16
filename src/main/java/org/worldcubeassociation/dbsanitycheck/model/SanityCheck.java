package org.worldcubeassociation.dbsanitycheck.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
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

	@OneToOne
	@JoinColumn(name = "sanity_check_category_id", referencedColumnName = "id")
	private SanityCheckCategory sanityCheckCategory;
}
