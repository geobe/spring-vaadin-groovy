package de.fh_zwickau.pti.geobe.service

import de.fh_zwickau.pti.geobe.domain.Project
import de.fh_zwickau.pti.geobe.domain.Sprint
import de.fh_zwickau.pti.geobe.domain.Task
import de.fh_zwickau.pti.geobe.dto.ProjectDto
import de.fh_zwickau.pti.geobe.dto.ProjectDto.CSet
import de.fh_zwickau.pti.geobe.dto.SprintDto
import de.fh_zwickau.pti.geobe.dto.TaskDto
import de.fh_zwickau.pti.geobe.repository.ProjectRepository
import de.fh_zwickau.pti.geobe.repository.SprintRepository
import de.fh_zwickau.pti.geobe.repository.TaskRepository
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

import javax.transaction.Transactional

/**
 * Facade class to access project entities
 * Created by georg beier on 16.11.2015.
 */
@Service
@Transactional
@Slf4j
class ProjectService {
    @Autowired
    private ProjectRepository projectRepository
    @Autowired
    private TaskRepository taskRepository
    @Autowired
    private SprintRepository sprintRepository
    @Autowired
    private TaskService taskService
//    @Autowired
//    private SprintService sprintService

    public ProjectDto.QList getProjects() {
        ProjectDto.QList qList = new ProjectDto.QList()
        projectRepository.findAll().sort {it.name.toLowerCase()}.each { Project p ->
            def node = new ProjectDto.QNode( [name: p.name])
            p.backlog.all.sort {it.tag.toLowerCase()}.each { Task t ->
                node.backlog.add(taskService.taskTree(t))
            }
            p.sprint.all.sort {it.start}.each { Sprint sp ->
                node.sprint.add(new SprintDto.QNode([id: sp.id, name: sp.name]))
            }
            qList.all[p.id] = node
        }
        qList
    }

    public String getProjectCaption(Long pid) {
        Project project = projectRepository.findOne(pid)
        if(project) {
            project.name
        } else {
            '--?--'
            log.error("no project found for id $pid")
        }
    }

    public ProjectDto.QFull getProjectDetails(Long pid) {
        Project p = projectRepository.findOne(pid)
        makeQFull(p)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ProjectDto.QFull createOrUpdateProject(CSet command) {
        Project project
        if (command.id) {
            project = projectRepository.findOne(command.id)
            if (!project) return new ProjectDto.QFull()
        } else {
            project = new Project()
        }
        project.name = command.name
        project.budget = command.budget
        if (command.taskIds)
            taskRepository.findAll(command.taskIds).forEach { Task t -> project.backlog.add(t) }
        if (command.sprintIds)
            sprintRepository.findAll(command.sprintIds).forEach { Sprint s -> project.sprint.add(s) }
        makeQFull(projectRepository.saveAndFlush(project))
    }

    private makeQFull(Project p) {
        if (p) {
            ProjectDto.QFull qFull = new ProjectDto.QFull(id: p.id, name: p.name, budget: p.budget)
            qFull.backlog = new TaskDto.QList()
            qFull.sprints = new SprintDto.QList()
            p.backlog.all.sort {it.tag.toLowerCase()}.forEach { Task t -> qFull.backlog.all[t.id] = t.tag }
            p.sprint.all.sort {it.start}.forEach { Sprint s -> qFull.sprints.all[s.id] = s.name }
            qFull
        } else {
            new ProjectDto.QFull()
        }
    }
}
