package de.fh_zwickau.pti.geobe.domain

import javax.persistence.Entity

/**
 * a task that is not composed from subtasks
 * Created by georg beier on 16.11.2015.
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
