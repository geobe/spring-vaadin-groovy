package de.fh_zwickau.pti.geobe.view

import com.vaadin.shared.ui.MarginInfo
import com.vaadin.spring.annotation.SpringComponent
import com.vaadin.spring.annotation.UIScope
import com.vaadin.ui.*
import com.vaadin.ui.themes.Reindeer
import de.fh_zwickau.pti.geobe.dto.SprintDto
import de.fh_zwickau.pti.geobe.dto.TaskDto.QNode
import de.fh_zwickau.pti.geobe.service.ProjectService
import de.fh_zwickau.pti.geobe.service.SprintService
import de.fh_zwickau.pti.geobe.util.view.VaadinSelectionListener
import de.fh_zwickau.pti.geobe.util.view.VaadinTreeRootChangeListener
import de.geobe.util.vaadin.TabViewStateMachine
import org.springframework.beans.factory.annotation.Autowired

import static TabViewStateMachine.Event
import static de.geobe.util.vaadin.VaadinBuilder.C
import static de.geobe.util.vaadin.VaadinBuilder.F

/**
 * show a sprint on its own tab
 * @author georg beier
 */
@SpringComponent
@UIScope
class SprintTab extends TabBase
        implements VaadinSelectionListener, VaadinTreeRootChangeListener, Serializable {
    private TextField name, project
    private DateField start, end
    private TwinColSelect backlog
    private Button newButton, editButton, saveButton, cancelButton
    private Map<String, Serializable> currentSprintItemId
    private Map<String, Serializable> currentProjectItemId
    private SprintDto.QFull currentDto


    @Autowired
    private SprintService sprintService
    @Autowired
    private ProjectService projectService

    @Override
    Component build() {
        Component c = vaadin."$C.gridlayout"('Sprints',
                [uikey  : 'grid', columns: 2, rows: 4,
                 margin : new MarginInfo(false, false, false, true),
                 spacing: true]) {
            "$F.text"('<b>Sprint</b>', [uikey: 'name', captionAsHtml: true, enabled: false])
            "$F.text"('Project', [uikey: 'project', enabled: false])
            "$F.date"('Start', [uikey: 'start', enabled: false, gridPosition: [0, 1]])
            "$F.date"('End', [uikey: 'end', enabled: false, gridPosition: [1, 1]])
            "$F.twincol"('Backlog', [uikey             : 'backlog', rows: 8, width: '100%',
                                     leftColumnCaption : 'available', enabled: false,
                                     rightColumnCaption: 'selected', gridPosition: [0, 2, 1, 2]])
            "$C.hlayout"([uikey: 'buttonfield', spacing: true, gridPosition: [0, 3, 1, 3]]) {
                "$F.button"('New', [uikey        : 'newbutton', disableOnClick: true,
                                    clickListener: { sm.execute(Event.Create) }])
                "$F.button"('Edit', [uikey        : 'editbutton', disableOnClick: true,
                                     clickListener: { sm.execute(Event.Edit) }])
                "$F.button"('Cancel', [uikey        : 'cancelbutton', disableOnClick: true, enabled: false,
                                       clickListener: { sm.execute(Event.Cancel) }])
                "$F.button"('Save', [uikey        : 'savebutton', disableOnClick: true, enabled: false,
//                                     clickShortcut: ShortcutAction.KeyCode.ENTER,
                                     styleName    : Reindeer.BUTTON_DEFAULT,
                                     clickListener: {
                                         sm.execute(Event.Save)
                                     }])
            }
        }
//        init()
        c
    }

    @Override
    void init(Object... value) {
        uiComponents = vaadin.uiComponents
        name = uiComponents."${subkeyPrefix}name"
        project = uiComponents."${subkeyPrefix}project"
        start = uiComponents."${subkeyPrefix}start"
        end = uiComponents."${subkeyPrefix}end"
        backlog = uiComponents."${subkeyPrefix}backlog"
        newButton = uiComponents."${subkeyPrefix}newbutton"
        editButton = uiComponents."${subkeyPrefix}editbutton"
        saveButton = uiComponents."${subkeyPrefix}savebutton"
        cancelButton = uiComponents."${subkeyPrefix}cancelbutton"
        projectTree.selectionModel.addListenerForKey(this, 'Sprint')
        projectTree.selectionModel.addRootChangeListener(this)
        // build state machine
        sm = new TabViewStateMachine(TabViewStateMachine.State.SUBTAB, 'SprTab')
        configureSm()
        sm.execute(Event.Init)
    }

    @Override
    void onItemSelected(Map<String, Serializable> sprintItemId) {
        currentSprintItemId = sprintItemId
        initItem((Long) sprintItemId['id'])
        sm.execute(Event.Select, sprintItemId['id'])
    }

    @Override
    void onRootChanged(Map<String, Serializable> projectItemId) {
        currentProjectItemId = projectItemId
        sm.execute(Event.Root)
    }

    @Override
    protected getCurrentItemId() { currentSprintItemId }

    @Override
    protected Long getCurrentDomainId() {(Long) currentSprintItemId['id']}

    @Override
    protected String getCurrentCaption() { currentDto.name }

    @Override
    protected getMatchForNewItem() {[type: ProjectTree.SPRINT_TYPE, id: currentDto.id]}

    /** prepare INIT state */
    @Override
    protected initmode() {
        [name, start, end, backlog, saveButton, cancelButton, editButton, newButton].each { it.enabled = false }
    }

    /** prepare EMPTY state */
    @Override
    protected emptymode() {
        clearFields()
        [name, start, end, backlog, saveButton, cancelButton, editButton].each { it.enabled = false }
        project.value = projectService.getProjectCaption((Long) currentProjectItemId['id'])
        setAvailableList()
        [newButton].each { it.enabled = true }
    }

    /** prepare SHOW state */
    @Override
    protected showmode() {
        [name, start, end, backlog, saveButton, cancelButton].each { it.enabled = false }
        [editButton, newButton].each { it.enabled = true }
    }

    /** prepare for editing in EDIT, CREATE, CREATEEMPTY states */
    @Override
    protected editmode() {
        projectTree.onEditItem()
        [name, start, end, backlog, saveButton, cancelButton].each { it.enabled = true }
        [editButton, newButton].each { it.enabled = false }
    }

    /** clear all editable fields */
    @Override
    protected clearFields() {
        [name, start, end, backlog].each { it.clear() }
    }

    private void setAvailableList() {
        backlog.removeAllItems()
        sprintService.getProjectBacklog((Long) currentProjectItemId['id']).each {
            makeAvailableList(it)
        }
    }

    private makeAvailableList(QNode taskNode, int level = 0) {
        backlog.addItem(taskNode.id)
        backlog.setItemCaption(taskNode.id, (level > 0 ? '  ' : '') + ('-' * level) + taskNode.tag)
        taskNode.children.each { makeAvailableList(it, level + 1) }
    }

    /**
     * for the given persistent object id, fetch the full dto and save it in field currentDto
     * @param itemId object id
     */
    @Override
    protected void initItem(Long itemId) {
        currentDto = sprintService.getSprintDetails(itemId)
        setFieldValues()
    }

    /**
     * set all fields from the current full dto object
     */
    @Override
    protected void setFieldValues() {
        name.value = currentDto.name
        project.value = currentDto.project.name
        start.value = currentDto.start
        end.value = currentDto.end
        setAvailableList()
        def select = []
        currentDto.backlog.all.each { k, v ->
            backlog.addItem(k)
            backlog.setItemCaption(k, v)
            select += k
        }
        backlog.setValue(select)
    }

    /**
     * create or update a domain object from the current field values and
     * update the current dto from the saved domain object
     *
     * @param id domain id of domain object or 0 (zero) to create a new
     * @return updated current dto
     */
    @Override
    protected saveItem(Long id) {
        SprintDto.CSet command = new SprintDto.CSet()
        command.id = id
        command.projectId = (Long) currentProjectItemId['id']
        command.name = name.getValue()
        command.start = start.getValue()
        command.end = end.value
        def v = []
        backlog.value.each {
            v << it
        }
        command.taskIds = v
        currentDto = sprintService.createOrUpdateSprint(command)
    }

}
