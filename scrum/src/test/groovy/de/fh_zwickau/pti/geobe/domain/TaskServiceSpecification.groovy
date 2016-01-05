package de.fh_zwickau.pti.geobe.domain

import de.fh_zwickau.pti.geobe.GroovaaApplication
import de.fh_zwickau.pti.geobe.dto.TaskDto
import de.fh_zwickau.pti.geobe.repository.ProjectRepository
import de.fh_zwickau.pti.geobe.repository.TaskRepository
import de.fh_zwickau.pti.geobe.service.StartupService
import de.fh_zwickau.pti.geobe.service.TaskService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import spock.lang.Specification

import javax.transaction.Transactional

/**
 *
 * Created by georg beier on 16.11.2015.
 */
@Slf4j
@SpringApplicationConfiguration(classes = GroovaaApplication)
class TaskServiceSpecification extends Specification {

    @Autowired
    private TaskRepository taskRepository
    @Autowired
    private ProjectRepository projectRepository
    @Autowired
    private TaskService taskService

    CompoundTask task1, toptask
    Subtask sub1, sub2, sub3
    Project project

    private fixture() {
        project = new Project(name: 'Spock', budget: 4711)
        sub1 = new Subtask(description: 'sub1', estimate: 1000, spent: 0, completed: false)
        sub2 = new Subtask(description: 'sub2', estimate: 2000, spent: 0, completed: false)
        sub3 = new Subtask(description: 'sub3', estimate: 3000, spent: 100, completed: true)
        toptask = new CompoundTask(description: 'toptask', estimate: 7000)
        task1 = new CompoundTask(description: 'compound1', estimate: 3000)
        [task1, sub1].each { it.supertask.add(toptask) }
        [sub2, sub3].each { it.supertask.add(task1) }
        [toptask, task1, sub3, sub1, sub2].each { it.project.add(project) }
        projectRepository.saveAndFlush(project)
//        taskRepository.save([sub1, sub2, sub3, task1, toptask])
//        taskRepository.flush()
    }

    @Autowired
    private StartupService startupService

    public cleanup() {
        startupService.cleanupAll()
    }

    @Transactional
    def 'TaskService builds correct task hierarchy tree'() {
        given: 'task hierarchy mapped to QNodes'
        fixture()
        TaskDto.QNode topNode = taskService.taskTree(toptask)
        when: 'hierarchy is mapped to Lists'
        def topid = topNode.id
        def l1ids = topNode.children.id
        def l2ids = topNode.children.children.id
        then:
        assert topid == toptask.id
        assert l1ids.flatten().containsAll([task1.id, sub1.id])
        assert l2ids.flatten().containsAll([sub2.id, sub3.id])
        cleanup:
        taskRepository.findAllCompoundTask().each { it.subtask.removeAll() }
    }

    @Transactional
    def 'subtask migration to compoundtask is correctly handled by TaskService'() {
        when: 'a CSet for a new Subtask'
        fixture()
        def wow = 42
        TaskDto.CSet cmd = new TaskDto.CSet()
        cmd.classname = 'Subtask'
        cmd.tag = 'quite new'
        cmd.description = 'newly created'
        cmd.estimate = wow
        cmd.completed = true
        cmd.supertaskId = sub1.id
//        cmd.projectId = project.id
        and: 'a subtask mutates to a compound task with a new sibbling'
        def newSub = taskService.createSubtask(cmd)
        and: 'the hierarchy tree with the new tasks is built'
        TaskDto.QNode topNode = taskService.taskTree(toptask)
        and: 'hierarchy is mapped to Lists'
        def topid = topNode.id
        def l1ids = topNode.children.id
        def l2ids = topNode.children.children.id
        then:
        assert newSub.supertask.all.size() == 1
        assert topid == toptask.id
        assert l1ids.flatten().containsAll([task1.id] + newSub.supertask.all.keySet())
        assert l2ids.flatten().containsAll([sub2.id, sub3.id, newSub.id])
        cleanup:
        def tasks = taskRepository.findAllCompoundTask()
        tasks.each { it.subtask.removeAll() }
    }

    @Transactional
    def 'subtask insertion to compoundtask is correctly handled by TaskService'() {
        when: 'a CSet for a new Subtask'
        fixture()
        def wow = 42
        TaskDto.CSet cmd = new TaskDto.CSet()
        cmd.classname = 'Subtask'
        cmd.tag = 'quite new'
        cmd.description = 'newly created'
        cmd.estimate = wow
        cmd.completed = true
        cmd.supertaskId = task1.id
//        cmd.projectId = project.id
        and: 'a compound task does not change with a new sibbling'
        def newSub = taskService.createSubtask(cmd)
        and: 'the hierarchy tree with the new tasks is built'
        TaskDto.QNode topNode = taskService.taskTree(toptask)
        and: 'hierarchy is mapped to Lists'
        def topid = topNode.id
        def l1ids = topNode.children.id
        def l2ids = topNode.children.children.id
        then:
        assert newSub.supertask.all.size() == 1
        assert topid == toptask.id
        assert l1ids.flatten().containsAll([task1.id, sub1.id])
        assert l2ids.flatten().containsAll([sub2.id, sub3.id, newSub.id])
        cleanup:
        def tasks = taskRepository.findAllCompoundTask()
        tasks.each { it.subtask.removeAll() }
    }
}
