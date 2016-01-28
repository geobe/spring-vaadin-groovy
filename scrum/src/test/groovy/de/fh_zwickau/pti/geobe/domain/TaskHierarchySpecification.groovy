package de.fh_zwickau.pti.geobe.domain

import de.fh_zwickau.pti.geobe.GroovaaApplication
import de.fh_zwickau.pti.geobe.repository.TaskRepository
import de.fh_zwickau.pti.geobe.service.StartupService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import spock.lang.Specification

import javax.transaction.Transactional

/**
 *
 * @author georg beier
 */
@Slf4j
@SpringApplicationConfiguration(classes = GroovaaApplication)
class TaskHierarchySpecification extends Specification {

    @Autowired
    private TaskRepository taskRepository

    CompoundTask task1, toptask
    Subtask sub1, sub2, sub3

    public setup() {
        sub1 = new Subtask(description: 'sub1', estimate: 1000, spent: 0, completed: false)
        sub2 = new Subtask(description: 'sub2', estimate: 2000, spent: 0, completed: false)
        sub3 = new Subtask(description: 'sub3', estimate: 3000, spent: 100, completed: true)
    }

    @Autowired
    private StartupService startupService

    public cleanup() {
        startupService.cleanupAll()
    }

    def "calculate compound values"() {
        when: "compoundTask are created"
        task1 = new CompoundTask(estimate: 5000)
        def task2 = new CompoundTask(sub3)
        and: 'subtasks are added'
        task1.subtask.add(sub1)
        task1.subtask.add(sub2)
        task1.subtask.add(task2)
        then: 'computed properties should be calculated'
        assert task1.subtask.all.size() == 3
        assert task1.summedEstimate == 6000
        assert task2.estimate == 3000
        assert !task1.completed
        assert task2.subtask.all.size() == 1
        assert task2.subtask.one == sub3
        assert task2.completed
        assert task2.summedEstimate == 3000
        assert task2.estimate == 3000
        assert sub3.supertask.one == task2
        assert sub3.estimate == 3000
    }

    @Transactional
    def 'task hierarchy can be persisted'() {
        when: "compoundTask are created"
        task1 = new CompoundTask(estimate: 5000)
        def task2 = new CompoundTask(sub3)
        and: 'subtasks are added'
        task1.subtask.add(sub1)
        task1.subtask.add(sub2)
        task1.subtask.add(task2)
        taskRepository.saveAndFlush(task1)
        then: 'all tasks should be persisted'
        assert taskRepository.count() == 5
    }
}
