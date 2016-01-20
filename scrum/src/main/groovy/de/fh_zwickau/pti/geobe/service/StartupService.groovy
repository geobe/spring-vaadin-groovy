package de.fh_zwickau.pti.geobe.service

import de.fh_zwickau.pti.geobe.domain.*
import de.fh_zwickau.pti.geobe.repository.ProjectRepository
import de.fh_zwickau.pti.geobe.repository.SprintRepository
import de.fh_zwickau.pti.geobe.repository.TaskRepository
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import javax.transaction.Transactional
import java.time.LocalDateTime

/**
 *
 * Created by georg beier on 09.11.2015.
 */
@Service
@Slf4j
class StartupService implements IStartupService {
    private boolean isInitialized = false

    @Autowired
    private ProjectRepository projectRepository
    @Autowired
    private TaskRepository taskRepository
    @Autowired
    private SprintRepository sprintRepository
    @Autowired
    private TaskService taskService

    @Override
    void initApplicationData() {
        if(!projectRepository.findAll() && !taskRepository.findAll() && ! sprintRepository.findAll()) {
            int cpl = 0
            log.info("initializing data at ${LocalDateTime.now()}")
            Project p = new Project([name: 'Projekt Küche', budget: 1000])
            p.backlog.add(new Subtask(tag: 'Tee kochen', description: 'Kanne zum Wasser!', estimate: 42))
            CompoundTask hausarbeit = new CompoundTask(tag: 'Hausarbeit', description: 'Immer viel zu tun', estimate: 4711)
            hausarbeit.project.add(p)
            p.backlog.add(hausarbeit)
            ['backen', 'kochen', 'abwaschen'].forEach {
                Task t = new CompoundTask([description: "Wir sollen $it", tag: it])
                t.supertask.add(hausarbeit)
//                t.project.add(p)
                cpl++
                ['dies', 'das', 'etwas anderes', 'nichts davon'].each { tag ->
                    def sub = new Subtask([description: "und dann noch $tag",
                                           tag        : tag, estimate: 250,
                                           completed  : (cpl % 2 == 0)])
                    t.subtask.add(sub)
                }
            }
            ['früh', 'mittag', 'abend'].each {
                new Sprint([name: it]).project.add(p)
            }
            projectRepository.saveAndFlush(p)
            p = new Project([name: 'Projekt Garten', budget: 2000])
            def tl = []
            ['umgraben', 'Rasen mähen', 'Äpfel pflücken', 'ernten'].forEach {
                Task t = new CompoundTask([description: "Wir sollen $it", tag: it])
                t.project.add(p)
                tl << t
            }
            int i = 0
            ['Frühling', 'Sommer', 'Herbst', 'Winter'].each {
                Sprint s = new Sprint([name: it])
                s.project.add(p)
                s.backlog.add(tl[(++i) % tl.size()])
                s.backlog.add(tl[(++i) % tl.size()])
            }
            projectRepository.saveAndFlush(p)
            def tasks = taskRepository.findAll()
            tasks.forEach({ log.info("task (${it.id}): $it.description") })
        }
    }

    @Override
    @Transactional
    void cleanupAll() {
        def projects = projectRepository.findAll()
        def tasks = taskRepository.findAll()
        tasks.each { Task t ->
            t.supertask.removeAll()
            t.project.removeAll()
            t.sprint.removeAll()
        }
        taskRepository.save(tasks)
        projects.each { Project p ->
            p.sprint.removeAll()
        }
        projectRepository.save(projects)
        projectRepository.deleteAll()
        taskRepository.deleteAll()
        sprintRepository.deleteAll()
    }
}
