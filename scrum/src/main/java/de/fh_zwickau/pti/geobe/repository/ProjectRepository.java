package de.fh_zwickau.pti.geobe.repository;

import de.fh_zwickau.pti.geobe.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by georg beier on 09.11.2015.
 */
public interface ProjectRepository extends JpaRepository<Project, Long> {
}
