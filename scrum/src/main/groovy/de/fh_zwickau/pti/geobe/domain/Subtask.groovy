package de.fh_zwickau.pti.geobe.domain

import javax.persistence.Entity

/**
 * a task that is not composed from subtasks
 * @author georg beier
 */
@Entity
class Subtask extends Task {
    long estimate
    long spent
    boolean completed

    long getSummedEstimate() {
        estimate
    }
}
