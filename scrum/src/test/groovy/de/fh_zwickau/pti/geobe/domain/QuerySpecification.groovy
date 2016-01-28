package de.fh_zwickau.pti.geobe.domain

import de.fh_zwickau.pti.geobe.GroovaaApplication
import de.fh_zwickau.pti.geobe.repository.ProjectRepository
import de.fh_zwickau.pti.geobe.repository.SprintRepository
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
class QuerySpecification extends Specification {

    @Autowired
    private ProjectRepository projectRepository
    @Autowired
    private TaskRepository taskRepository
    @Autowired
    private SprintRepository sprintRepository
    @Autowired StartupService startupService

    Project project
    CompoundTask task

    def setup() {
        startupService.initApplicationData()
    }

    @Transactional
    def 'a task query for a specified sprint'() {
        given:
        def projects = projectRepository.findAll()
        def pid = projects[1]?.id
        def sprints = sprintRepository.findAll()
        def tasks = taskRepository.findByProjectId(pid)
        def s0 = sprints[0]
        def s1 = sprints[1]
        def t0 = tasks[0]
        def t1 = tasks[1]
        def t2 = tasks[2]
        def tc = tasks.size()
        when:
        log.info("${sprints.size()} sprints, ${tasks.size()} tasks")
        s1.backlog.add(t0)
        s1.backlog.add(t1)
        s1.backlog.add(t2)
        log.info("${taskRepository.findByProjectIdAndIdNotIn(pid, [t0.id, t1.id])*.tag}")
        taskRepository.save([t0, t1, t2])
        sprintRepository.save(s1)
        then:
        assert sprints.size() > 0
        assert tasks.size() > 0
        assert projects.size() > 0
        assert taskRepository.findBySprintsId(s1.id) == [t0, t1, t2]
        assert taskRepository.findBySprintsIdAndIdNotIn(s1.id, [t1.id, t2.id]) == [t0]
        assert taskRepository.findByProjectIdAndIdNotIn(pid, [t0.id, t1.id]).contains(t2)
    }

    def 'a compound task native query'() {
        when:
        def ct = taskRepository.findAllCompoundTask()
        def st = taskRepository.findAllSubtask()
        then:
        assert ct
        assert st
    }

}
