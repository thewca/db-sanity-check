package org.worldcubeassociation.dbsanitycheck.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.worldcubeassociation.dbsanitycheck.model.SanityCheck;

public interface SanityCheckRepository extends JpaRepository<SanityCheck, Long> {

}
