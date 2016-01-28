package de.fh_zwickau.pti.geobe.repository;

import de.fh_zwickau.pti.geobe.domain.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * The standard Spring repository interface
 * @author georg beier
 */
public interface SprintRepository extends JpaRepository<Sprint, Long> {
    List<Sprint> findAllByOrderByStartDesc();
}
