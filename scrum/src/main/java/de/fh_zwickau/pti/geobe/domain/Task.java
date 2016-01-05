package de.fh_zwickau.pti.geobe.domain;

import de.geobe.util.association.IToAny;
import de.geobe.util.association.ToMany;
import de.geobe.util.association.ToOne;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * An abstract superclass for tasks
 * Created by georg beier on 16.11.2015.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String tag = "";
    private String description = "Task ist noch nicht beschrieben";

    public Long getId() {
        return id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public abstract long getEstimate();

    public abstract long getSummedEstimate();

    public abstract long getSpent();

    public abstract boolean isCompleted();

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;
    @Transient
    private ToOne<Task, Project> toProject = new ToOne<>(
            () -> project, (Project p) -> project = p,
            this, Project::getBacklog);

    public IToAny<Project> getProject() {
        return toProject;
    }

    @ManyToMany(mappedBy = "backlog")
    private Set<Sprint> sprints = new HashSet<>();
    @Transient
    private ToMany<Task, Sprint> toSprint = new ToMany<>(
            () -> sprints, this, Sprint::getBacklog);

    public IToAny<Sprint> getSprint() {
        return toSprint;
    }

//    @OneToMany(mappedBy = "supertask", cascade = CascadeType.PERSIST)
//    private Set<Task> subtasks = new HashSet<>();
//    @Transient
//    private ToMany<Task, Task> toSubtask = new ToMany<Task, Task>(
//            () -> subtasks, this, Task::getSupertask);
//
//    public IToAny<Task> getSubtask() {
//        return toSubtask;
//    }
//
    @ManyToOne
    @JoinColumn(name = "supertask_id")
    private CompoundTask supertask;
    @Transient
    private ToOne<Task, CompoundTask> toSupertask = new ToOne<>(
            () -> supertask, (CompoundTask t) -> supertask = t,
            this, CompoundTask::getSubtask);

    public IToAny<CompoundTask> getSupertask() {
        return toSupertask;
    }
}
