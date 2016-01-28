package de.fh_zwickau.pti.geobe.repository;

import de.fh_zwickau.pti.geobe.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author georg beier
 */
public interface ProjectRepository extends JpaRepository<Project, Long> {
}
