package de.fh_zwickau.pti.geobe.service

import de.fh_zwickau.pti.geobe.domain.Project
import de.fh_zwickau.pti.geobe.domain.Sprint
import de.fh_zwickau.pti.geobe.domain.Task
import de.fh_zwickau.pti.geobe.dto.ProjectDto
import de.fh_zwickau.pti.geobe.dto.SprintDto
import de.fh_zwickau.pti.geobe.dto.SprintDto.CSet
import de.fh_zwickau.pti.geobe.dto.TaskDto
import de.fh_zwickau.pti.geobe.repository.ProjectRepository
import de.fh_zwickau.pti.geobe.repository.SprintRepository
import de.fh_zwickau.pti.geobe.repository.TaskRepository
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import javax.transaction.Transactional

/**
 * Facade class to access sprint entities
 * Created by georg beier on 16.11.2015.
 */
@Slf4j
@Service
@Transactional
class SprintService {
    @Autowired
    private ProjectRepository projectRepository
    @Autowired
    private TaskRepository taskRepository
    @Autowired
    private SprintRepository sprintRepository
    @Autowired
    private TaskService taskService

    public SprintDto.QList getSprints() {
        SprintDto.QList qList = new SprintDto.QList()
        sprintRepository.findAllByOrderByStartDesc().each { Sprint sp ->
            def node = new SprintDto.QNode([name: sp.name])
            sp.backlog.all.sort { it.tag.toLowerCase() }.each { Task t ->
                node.backlog = taskService.taskTree(t)
            }
            qList.all[sp.id] = node
        }
        qList
    }

    public List<TaskDto.QNode> getProjectBacklog(Long pid) {
        List<TaskDto.QNode> nodes = []
        Project p = projectRepository.findOne(pid)
        p.backlog.all.sort { it.tag.toLowerCase() }.each { Task t ->
            if (!t.completed)
                nodes << taskService.taskTree(t)
        }
        nodes
    }

    public SprintDto.QFull getSprintDetails(Long pid) {
        Sprint sp = sprintRepository.findOne(pid)
        makeQFull(sp)
    }

    public SprintDto.QFull createOrUpdateSprint(CSet command) {
        Sprint sp
        if (command.id) {
            sp = sprintRepository.findOne(command.id)
            if (!sp) {
                log.error("cannot find sprint for id $command.id")
                return new SprintDto.QFull()
            }
        } else {
            sp = new Sprint()
            Project p = projectRepository.findOne(command.projectId)
            if (!p) {
                log.error("cannot find project for id $command.projectId")
                return new SprintDto.QFull()
            }
            sp.project.add(p)
        }
        sp.name = command.name
        sp.start = command.start
        sp.end = command.end
        taskRepository.findAll(command.taskIds)
                .sort { it.tag.toLowerCase() }.each { Task t -> sp.backlog.add(t) }
        // findBySprintsIdAndIdNotIn does not work with an empty list of ids, so add an invalid id 0
        taskRepository.findBySprintsIdAndIdNotIn(sp.id, command.taskIds ?: [0L])
                .sort { it.tag.toLowerCase() }.each { Task t ->
            sp.backlog.remove(t)
        }
        makeQFull(projectRepository.saveAndFlush(sp))
    }

    private makeQFull(Sprint sp) {
        if (sp) {
            SprintDto.QFull qFull =
                    new SprintDto.QFull(id: sp.id, name: sp.name, start: sp.start, end: sp.end)
            Project p = sp.project.one
            qFull.project = new ProjectDto.QNode(name: p.name)
            def assigned = []
            sp.backlog.all.forEach { Task t ->
                qFull.backlog.all[t.id] = t.tag
                assigned.add(t.id)
            }
            // findByProjectIdAndIdNotIn does not work with an empty list of ids, so add an invalid id 0
//            taskRepository.findByProjectIdAndIdNotIn(sp.project.one.id, assigned ?: [0L]).each { Task t ->
//                qFull.available.all[t.id] = t.tag
//            }
            qFull
        } else {
            new SprintDto.QFull()
        }
    }
}
