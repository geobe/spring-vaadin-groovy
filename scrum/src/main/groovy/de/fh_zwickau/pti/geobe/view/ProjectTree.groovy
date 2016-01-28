package de.fh_zwickau.pti.geobe.view

import com.vaadin.data.Property
import com.vaadin.spring.annotation.SpringComponent
import com.vaadin.spring.annotation.UIScope
import com.vaadin.ui.Component
import com.vaadin.ui.Tree
import de.fh_zwickau.pti.geobe.service.ProjectService
import de.fh_zwickau.pti.geobe.util.view.VaadinSelectionModel
import de.fh_zwickau.pti.geobe.util.view.VaadinTreeHelper
import de.geobe.util.vaadin.SubTree
import org.springframework.beans.factory.annotation.Autowired
import org.vaadin.spring.security.VaadinSecurity

import static de.geobe.util.vaadin.VaadinBuilder.C
import static de.geobe.util.vaadin.VaadinBuilder.F

/**
 * Main selection component is this tree
 *
 * @author georg beier
 */
@SpringComponent
@UIScope
class ProjectTree extends SubTree
        implements Serializable {

    public static final String PROJECT_TYPE = 'Project'
    public static final String SPRINT_TYPE = 'Sprint'
    public static final String TASK_TYPE = 'Task'

    private static final String PTREE = 'ptree'
    private static final String MENU = 'logoutmenu'
    private Tree projectTree
    private VaadinTreeHelper treeHelper

    private uiComponents

    private Map<String, Serializable> selectedProjectId

    def getSelectedProjectId() { selectedProjectId }

    @Autowired
    private ProjectService projectService

    @Autowired
    private VaadinSecurity vaadinSecurity

    VaadinSelectionModel selectionModel = new VaadinSelectionModel()

    @Override
    Component build() {
        vaadin."$C.vlayout"() {
            "$F.menubar"([uikey: MENU]) {
                "$F.menuitem"('Logout', [command: { vaadinSecurity.logout() }])
            }
            "$C.panel"('Projekte', [spacing: true, margin: true]) {
                "$F.tree"('Projekte, Backlogs und Sprints',
                        [uikey: PTREE, caption: 'MenuTree',
                         valueChangeListener: {treeValueChanged(it)}])
            }
        }
    }

    @Override
    void init(Object... value) {
        uiComponents = vaadin.uiComponents
        projectTree = uiComponents."${subkeyPrefix + PTREE}"
        treeHelper = new VaadinTreeHelper(projectTree)
        buildTree(projectTree)
    }

    /**
     * handle changes of tree selection
     * @param event info on the newly selected tree item
     */
    private void treeValueChanged(Property.ValueChangeEvent event) {
        def selectId = event.property.value
        if (selectId) {
            def topItemId = treeHelper.topParentForId(selectId)
            if (topItemId != selectedProjectId) {
                selectionModel.notifyRootChange(topItemId)
                selectedProjectId = topItemId
            }
            if (selectId instanceof Map) {
                selectionModel.notifyChange(selectId)
            }
        }
    }

    /**
     * build a tree representing the domain model
     * @param projectTree
     */
    private void buildTree(Tree projectTree) {
        def projects = projectService.projects
        //loop over all projects
        projects.all.each { projId, projNode ->
            def projectId = treeHelper.addNode([type: PROJECT_TYPE, id: projId],
                    null, projNode.name, true)
            // an intermediate node 'backlog'
            def backlogTagId = treeHelper.addNode('backlog:' + projId, projectId,
                    'backlog', !projNode.backlog.isEmpty())
            if (projNode.backlog) {
                // build a subtree for every backlog task
                projNode.backlog.each { taskNode ->
                    treeHelper.descend(taskNode, backlogTagId, TASK_TYPE, 'id',
                            'tag', 'children')
                }
            }
            def sprintsTagId = treeHelper.addNode('sprints:' + projId, projectId,
                    'sprints', !projNode.sprint.isEmpty())
            if (projNode.sprint) {
                projNode.sprint.each { sprintNode ->
                    treeHelper.addNode([type: SPRINT_TYPE, id: sprintNode.id],
                            sprintsTagId, sprintNode.name, false)
                }
            }

        }
    }

    /**
     * disable the tree while a tree item is edited on one of the tab pages
     */
    public void onEditItem() {
        projectTree.enabled = false
    }

    /**
     * enable and update the tree after editing an item
     * @param itemId identifies edited item
     * @param caption eventually updated caption of the edited item
     * @param mustReload tree must reload after new item was created
     *        or structure changed
     */
    public void onEditItemDone(Object itemId, String caption, boolean mustReload = false) {
        if (mustReload) {
            def expandedNodes = treeHelper.allExpanded
            projectTree.removeAllItems()
            buildTree(projectTree)
            def select = treeHelper.findMatchingId(itemId)
            if (select)
                projectTree.select(select)
            treeHelper.reexpand(expandedNodes)
        } else {
            if (projectTree.getItemCaption(itemId) != caption) {
                projectTree.setItemCaption(itemId, caption)
            }
        }
        projectTree.enabled = true
    }
}