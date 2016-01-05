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
 * Created by georg beier on 09.11.2015.
 */
@Slf4j
@SpringApplicationConfiguration(classes = GroovaaApplication)
class JavaGroovyEntitySpecification extends Specification {

    @Autowired
    private ProjectRepository projectRepository
    @Autowired
    private TaskRepository taskRepository
    @Autowired
    private SprintRepository sprintRepository

    Project project1
    Task task1, task2
    Sprint sprint1, sprint2, sprint3

    public setup() {
        project1 = new Project([name: 'Projekt 1', budget: 10000])
        task1 = new CompoundTask([description: "eine neue Aufgabe"])
        task2 = new Subtask([description: 'nee das mach ich nich', tag: 'bäääh'])
        sprint1 = new Sprint([name: 'sprint 1'])
        sprint2 = new Sprint([name: 'sprint 2'])
        sprint3 = new Sprint([name: 'sprint 3'])
        log.info("setup called")
    }

    @Autowired
    private StartupService startupService

    public cleanup() {
        startupService.cleanupAll()
    }

    def "save and retrieve groovy entity"() {
        setup: 'clean initialized db'
        cleanup()
        when: " entities are saved"
        sprintRepository.save([sprint1, sprint2, sprint3])
        sprintRepository.flush()
        taskRepository.save([task1, task2])
//        taskRepository.save task2
        taskRepository.flush()
        then: 'db should contain these entities'
        assert sprintRepository.count() == 3
        assert taskRepository.count() == 2
        assert taskRepository.findOne(task1.id) instanceof CompoundTask
        assert taskRepository.findOne(task2.id) instanceof Subtask
    }

    @Transactional
    def "integration of java and groovy entities: Sprint - Project" () {
        when: "sprints are added to a project"
        project1.sprint.add(sprint1)
        project1.sprint.add(sprint2)
        project1.sprint.add(sprint3)
        and: 'project is saved'
        projectRepository.saveAndFlush(project1)
        and: 'objects are retrieved back from db'
        def pdb = projectRepository.findAll()[0]
        def sdb = pdb?.sprint?.one
        then:
        assert sprintRepository.count() == 3
        assert pdb
        assert sdb
        assert pdb.sprint.all.size() == 3
        assert sdb.project.one == pdb
        assert pdb == project1
        assert sdb in [sprint1, sprint2, sprint3]
    }

    @Transactional
    def "integration of java and groovy entities: Sprint - Task" () {
        when: "sprints are added to a task"
        task1.sprint.add(sprint1)
        task1.sprint.add(sprint2)
        task1.sprint.add(sprint3)
        and: 'sprints and task are saved'
        sprintRepository.save([sprint1, sprint2, sprint3])
        taskRepository.saveAndFlush(task1)
        and: 'objects are retrieved back from db'
        def taskdb = taskRepository.findAll()[0]
        def sprintdb = taskdb?.sprint?.one
        then:
        assert sprintRepository.count() == 3
        assert taskdb
        assert sprintdb
        assert taskdb.sprint.all.size() == 3
        assert sprintdb.backlog.one == taskdb
        assert taskdb == task1
        assert sprintdb in [sprint1, sprint2, sprint3]
    }

    @Transactional
    def 'removeAll clears association'() {
        when: "sprints are added to a project"
        project1.sprint.add(sprint1)
        project1.sprint.add(sprint2)
        project1.sprint.add(sprint3)
        and: 'project is saved'
        projectRepository.saveAndFlush(project1)
        and: 'objects are retrieved back from db'
        def pdb = projectRepository.findAll()[0]
        and: "association is cleared"
        project1.sprint?.removeAll()
        projectRepository.saveAndFlush(pdb)
        then: "association is really clear"
        assert project1.sprint.all.size() == 0
        assert sprint1.project.one == null
        assert pdb.sprint.all.size() == 0
    }

}
