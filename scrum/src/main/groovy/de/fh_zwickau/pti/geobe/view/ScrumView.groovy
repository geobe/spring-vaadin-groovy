package de.fh_zwickau.pti.geobe.view

import com.vaadin.annotations.Theme
import com.vaadin.server.ErrorEvent
import com.vaadin.server.VaadinRequest
import com.vaadin.spring.annotation.SpringUI
import com.vaadin.ui.Component
import com.vaadin.ui.Notification
import com.vaadin.ui.TabSheet
import com.vaadin.ui.UI
import de.fh_zwickau.pti.geobe.util.view.VaadinSelectionKeyListener
import de.geobe.util.vaadin.VaadinBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.vaadin.spring.security.util.SecurityExceptionUtils

import static VaadinBuilder.C
import static VaadinBuilder.F

/**
 * The main view class for Scrum UI
 * Created by georg beier on 16.11.2015.
 */
@SpringUI(path = "")
@Theme("valo")
class ScrumView extends UI implements VaadinSelectionKeyListener {

    def VaadinBuilder vaadin
    def widgets = [:]
    @Autowired
    private ProjectTab projectTab
    @Autowired
    private TaskTab taskTab
    @Autowired
    private SprintTab sprintTab
    @Autowired
    ProjectTree projectTree

    private Component root, projectSelectTree,
            projectSubtree, sprintSubtree, taskSubtree

    @Override
    protected void init(VaadinRequest request) {
        page.title = 'spring-vaadin-groovy demo'
        setContent(initBuilder())
        initComponents()
        errorHandler = { handleError(it) }
    }

    /**
     * Aufbau des Vaadin Komponentenbaums<br>
     *     "Äste" werden vor dem "Stamm" angelegt und mit subtree hinzugefügt werden
     * @return
     */
    Component initBuilder() {
        vaadin = new VaadinBuilder()
        projectSelectTree = projectTree.buildSubtree(vaadin, 'menutree.')
        projectSubtree = projectTab.buildSubtree(vaadin, 'project.')
        sprintSubtree = sprintTab.buildSubtree(vaadin, 'sprint.')
        taskSubtree = taskTab.buildSubtree(vaadin, 'task.')

        root = vaadin."$C.hsplit"([uikey: 'topsplit', splitPosition: 20.0f]) {
            "$F.subtree"(projectSelectTree, [uikey: 'menu'])
            "$C.tabsheet"([uikey: 'tabs']) {
                "$F.subtree"(projectSubtree, [uikey: 'projectpanel'])
                "$F.subtree"(sprintSubtree, [uikey: 'sprintpanel'])
                "$F.subtree"(taskSubtree, [uikey: 'taskpanel'])
            }
        }
        widgets = vaadin.uiComponents
//        def wtree = vaadin.toString()
//        println wtree
        root
    }

    private initComponents(){
        // untergeordnete views erst nach Zusammenbau der ganzen UI initialisieren
        projectTree.init()
        projectTab.init()
        sprintTab.init()
        taskTab.init()
        projectTree.selectionModel.addKeyListener(this)
    }

    private void handleError(def event){
        if (SecurityExceptionUtils.isAccessDeniedException(event.getThrowable())) {
            Notification.show("Sorry, you don't have access to do that.");
        } else {
            Notification.show("Something went wrong: $event");
        }

    }
/**
 * is fired when an entry of a selection component was selected
 * @param event id of the selected element, normally its domain class itemId
 */
    @Override
    void onItemKeySelected(Map<String, Serializable> itemId) {
        TabSheet tabs = widgets['tabs']
        switch (itemId['type']) {
            case 'Project':
            case 'project':
                tabs.selectedTab = projectSubtree
                break
            case 'Task':
            case 'task':
                tabs.selectedTab = taskSubtree
                break
            case 'Sprint':
            case 'sprint':
                tabs.selectedTab = sprintSubtree
                break
        }
    }
}
