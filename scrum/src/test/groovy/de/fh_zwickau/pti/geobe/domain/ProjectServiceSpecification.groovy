package de.fh_zwickau.pti.geobe.domain

import de.fh_zwickau.pti.geobe.GroovaaApplication
import de.fh_zwickau.pti.geobe.dto.ProjectDto
import de.fh_zwickau.pti.geobe.repository.ProjectRepository
import de.fh_zwickau.pti.geobe.repository.SprintRepository
import de.fh_zwickau.pti.geobe.repository.TaskRepository
import de.fh_zwickau.pti.geobe.service.ProjectService
import de.fh_zwickau.pti.geobe.service.StartupService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.http.MediaType
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.vaadin.spring.security.VaadinSecurity
import spock.lang.Specification

import javax.transaction.Transactional

/**
 *
 * @author georg beier
 */
@SpringApplicationConfiguration(classes = GroovaaApplication)
class ProjectServiceSpecification extends Specification {

    @Autowired
    private ProjectRepository projectRepository
    @Autowired
    private TaskRepository taskRepository
    @Autowired
    private SprintRepository sprintRepository
    @Autowired
    private ProjectService projectService
    @Autowired
    private  VaadinSecurity vaadinSecurity
    Project project
    CompoundTask task
    Subtask stask

    public setup() {
        project = new Project()
        project.name = "ein Projekt"
        task = new CompoundTask([description: "eine neue Aufgabe", tag: 'Guten Tag'])
        stask = new Subtask([description: 'ohne Worte', tag: 'blah'])
    }

    @Autowired
    private StartupService startupService

    public cleanup() {
        startupService.cleanupAll()
    }

    def "get a dto from a project"() {
        setup:
        cleanup()
        when: 'a project with a task is in the database'
        project.getBacklog().add(task)
        project.getBacklog().add(stask)
        projectRepository.saveAndFlush(project)
        and: 'we ask for query dtos'
        ProjectDto.QList qList = projectService.projects
        ProjectDto.QFull qFull = projectService.getProjectDetails(project.id)
        then:
        assert qList.all.size() == 1
        assert qList.all.keySet().contains(project.id)
        assert qList.all.values().name.any {it == project.name }
        assert qFull.id == project.id
        assert qFull.name == project.name
        assert qFull.budget == project.budget
        assert qFull.backlog.all.size() == 2
        assert qFull.sprints.all.size() == 0
        assert qFull.backlog.all[task.id] == task.tag
        assert qFull.backlog.all[stask.id] == stask.tag
    }

    @Transactional
    def "create a new project from a dto"() {
        given: 'a new CSet command object'
        ProjectDto.CSet cSet = new ProjectDto.CSet([name: 'a new project', budget: 5000])
        and: 'two tasks in the repository'
        def tasks = [task] << new Subtask([tag: 'subtask1', estimate: 300, spent: 123, completed: false])
        taskRepository.save(tasks)
        when: 'we call the project service with that command'
        tasks.forEach {cSet.taskIds << it.id}
        ProjectDto.QFull qFull = projectService.createOrUpdateProject(cSet)
        then: 'Project should be saved and QFull populated'
        assert projectRepository.findAll().size() == 1
        assert qFull.id
        assert qFull.backlog.all.size() == 2
        assert projectRepository.findOne(qFull.id).name == qFull.name
        assert projectRepository.findOne(qFull.id).budget == qFull.budget
        assert projectRepository.findOne(qFull.id).backlog.all.size() == 2
    }


}
