package org.worldcubeassociation.dbsanitycheck.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "sanity_check_categories")
public class Category {
    @Id
    private Integer id;

    private String name;

    @Column(name = "email_to")
    private String emailTo;
}
