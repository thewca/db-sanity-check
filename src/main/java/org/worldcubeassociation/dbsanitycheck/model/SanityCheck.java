package org.worldcubeassociation.dbsanitycheck.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "sanity_checks")
public class SanityCheck {
	@Id
	private Integer id;

	private String topic;

	private String comments;

	private String query;

	@Column(name = "sanity_check_category_id", insertable = false, updatable = false)
	private Integer sanityCheckCategoryId;

	@OneToOne
	@JoinColumn(name = "sanity_check_category_id", referencedColumnName = "id")
	private SanityCheckCategory sanityCheckCategory;

	@OneToMany(fetch = FetchType.EAGER)
	@JoinColumn(name = "sanity_check_id", referencedColumnName = "id")
	private List<SanityCheckExclusion> exclusions;
}
