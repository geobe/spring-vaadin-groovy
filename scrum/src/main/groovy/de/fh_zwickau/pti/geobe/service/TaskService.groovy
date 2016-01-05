package de.fh_zwickau.pti.geobe.service

import de.fh_zwickau.pti.geobe.domain.CompoundTask
import de.fh_zwickau.pti.geobe.domain.Project
import de.fh_zwickau.pti.geobe.domain.Subtask
import de.fh_zwickau.pti.geobe.domain.Task
import de.fh_zwickau.pti.geobe.dto.ProjectDto
import de.fh_zwickau.pti.geobe.dto.SprintDto
import de.fh_zwickau.pti.geobe.dto.TaskDto
import de.fh_zwickau.pti.geobe.dto.TaskDto.CSet
import de.fh_zwickau.pti.geobe.repository.ProjectRepository
import de.fh_zwickau.pti.geobe.repository.SprintRepository
import de.fh_zwickau.pti.geobe.repository.TaskRepository
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import javax.transaction.Transactional

/**
 *
 * Created by georg beier on 09.11.2015.
 */
@Slf4j
@Service
@Transactional
class TaskService {
    @Autowired
    private ProjectRepository projectRepository
    @Autowired
    private TaskRepository taskRepository
    @Autowired
    private SprintRepository sprintRepository

    TaskDto.QList getTasks() {
        TaskDto.QList qList = new TaskDto.QList()
        taskRepository.findAllByOrderByTagAsc().each { qList.all[it.id] = it.tag }
        qList
    }

    TaskDto.QFull getTaskDetails(Long id) {
        Task t = taskRepository.findOne(id)
        makeQFull(t)
    }

    TaskDto.QFull createSubtask(CSet cmd) {
        Task t = taskRepository.findOne(cmd.supertaskId)
        if (t instanceof Subtask) {
            CompoundTask ct = new CompoundTask(t)
            taskRepository.saveAndFlush(ct)
            Task st = ct.supertask.one
            if (st) {
                taskRepository.saveAndFlush(st)
            }
            t.supertask.removeAll()
            t.project.removeAll()
            taskRepository.delete(t)
            cmd.supertaskId = ct.id
        }
        createOrUpdate(cmd)
    }

    TaskDto.QFull createOrUpdate(CSet cmd) {
        Task task
        if (cmd.id) {
            task = taskRepository.findOne(cmd.id)
            if (!task) {
                log.error("no task found for id $cmd.id")
                return new TaskDto.QFull()
            }
            task.tag = cmd.tag
            task.estimate = cmd.estimate
            task.description = cmd.description
            if (task instanceof Subtask) {
                task.spent = cmd.spent
                task.completed = cmd.completed
            }
        } else if (cmd.projectId || cmd.supertaskId) {
            if (cmd.classname.endsWith('Subtask')) {
                task = new Subtask()
                task.tag = cmd.tag ?: 'tagless'
                task.description = cmd.description ?: ''
                task.estimate = cmd.estimate ?: 0
                task.spent = cmd.spent ?: 0
                task.completed = cmd.completed ?: false
            } else {
                task = new CompoundTask()
                task.tag = cmd.tag ?: 'tagless'
                task.description = cmd.description ?: ''
                task.estimate = cmd.estimate ?: 0
            }
            if (cmd.projectId) {
                Project p = projectRepository.findOne(cmd.projectId)
                if (p) {
                    task.project.add(p)
                } else {
                    log.error("no project found for $cmd.projectId")
                    return new TaskDto.QFull()
                }
            }
        } else {
            log.error("no project or supertask defined for new task")
            return new TaskDto.QFull()
        }
        if (cmd.subtaskIds)
            taskRepository.findAllByOrderByTagAsc(cmd.subtaskIds).forEach { task.subtask.add(it) }
        if (cmd.supertaskId)
            task.supertask.add(taskRepository.findOne(cmd.supertaskId))
        if (cmd.sprintIds)
            sprintRepository.findAll(cmd.sprintIds).forEach { task.sprint.add(it) }
        makeQFull(projectRepository.saveAndFlush(task))
    }

    public TaskDto.QNode taskTree(Task t, boolean notCompleted = false) {
        new TaskDto.QNode([id: t.id, tag: t.tag, children: taskSubtree(t, notCompleted)])
    }

    private makeQFull(Task t) {
        if (t) {
            TaskDto.QFull qFull = new TaskDto.QFull([
                    id       : t.id, tag: t.tag, description: t.description,
                    estimate : t.estimate, spent: t.spent,
                    completed: t.completed])
            qFull.classname = t.class.canonicalName.replaceAll(/.*\./, '')
            qFull.project = new ProjectDto.QList()
            qFull.supertask = new TaskDto.QList()
            qFull.sprints = new SprintDto.QList()
            t.project.all.each { qFull.project.all[it.id] = it.name }
            t.supertask.all.each { qFull.supertask.all[it.id] = it.tag }
            t.sprint.all.sort {it.start}.each { qFull.sprints.all[it.id] = it.name }
            qFull.subtasks = taskSubtree(t)
            qFull
        } else {
            new TaskDto.QFull()
        }
    }

    private List<TaskDto.QNode> taskSubtree(Task t, boolean notCompleted = false) {
        List<TaskDto.QNode> subtree = new LinkedList<>()
        if(t instanceof CompoundTask) {
            t.subtask.all.sort { it.tag.toLowerCase() }.each { Task subtask ->
                if (!(subtask.completed && notCompleted)) {
                    TaskDto.QNode node = new TaskDto.QNode([id: subtask.id, tag: subtask.tag])
                    node.children = taskSubtree(subtask)
                    subtree.add(node)
                }
            }
        }
        subtree
    }
}


