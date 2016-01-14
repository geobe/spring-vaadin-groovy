package de.fh_zwickau.pti.geobe.view

import com.vaadin.event.ShortcutAction
import com.vaadin.spring.annotation.SpringComponent
import com.vaadin.spring.annotation.UIScope
import com.vaadin.ui.Button
import com.vaadin.ui.Component
import com.vaadin.ui.Notification
import com.vaadin.ui.TextField
import com.vaadin.ui.themes.Reindeer
import de.fh_zwickau.pti.geobe.dto.ProjectDto
import de.fh_zwickau.pti.geobe.service.IAuthorizationService
import de.fh_zwickau.pti.geobe.service.ProjectService
import de.fh_zwickau.pti.geobe.util.view.VaadinSelectionListener
import de.fh_zwickau.pti.geobe.util.view.VaadinTreeRootChangeListener
import de.geobe.util.vaadin.TabViewStateMachine
import org.springframework.beans.factory.annotation.Autowired

import static TabViewStateMachine.Event
import static de.geobe.util.vaadin.VaadinBuilder.C
import static de.geobe.util.vaadin.VaadinBuilder.F

/**
 * show and edit projects in their own tab
 *
 * Created by georg beier on 16.11.2015.
 */
@SpringComponent
@UIScope
class ProjectTab extends TabBase implements VaadinSelectionListener,
        VaadinTreeRootChangeListener, Serializable {

    private static final String PID = 'pid'
    private static final String PNAME = 'pname'
    private static final String PBUDGET = 'pbudget'

    private TextField pid, pname, pbudget
    private Button newButton, editButton, saveButton, cancelButton
    private Map<String, Serializable> currentItemId
    private ProjectDto.QFull currentDto

    @Autowired
    private ProjectService projectService
    @Autowired
    private ProjectTree projectTree
    @Autowired
    private IAuthorizationService authorizationService

    @Override
    Component build() {
        // Caption shows on the tab
        def c = vaadin."$C.vlayout"('Projekt',
                [spacing: true, margin: true]) {
            "$F.text"('ID', [uikey: PID, enabled: false])
            "$F.text"('Name', [uikey: PNAME])
            "$F.text"('Budget', [uikey: PBUDGET])
            "$C.hlayout"([uikey       : 'buttonfield', spacing: true,
                          gridPosition: [0, 3, 1, 3]]) {
                "$F.button"('New', [uikey         : 'newbutton',
                                    visible       : authorizationService.hasRole('ROLE_ADMIN'),
                                    disableOnClick: true,
                                    clickListener : { sm.execute(Event.Create) }])
                "$F.button"('Edit', [uikey         : 'editbutton',
                                     visible       : authorizationService.hasRole('ROLE_ADMIN'),
                                     disableOnClick: true,
                                     clickListener : { sm.execute(Event.Edit) }])
                "$F.button"('Cancel', [uikey         : 'cancelbutton',
                                       disableOnClick: true, enabled: false,
                                       clickListener : { sm.execute(Event.Cancel) }])
                "$F.button"('Save', [uikey         : 'savebutton',
                                     disableOnClick: true, enabled: false,
                                     clickShortcut : ShortcutAction.KeyCode.ENTER,
                                     styleName     : Reindeer.BUTTON_DEFAULT,
                                     clickListener : { sm.execute(Event.Save) }])
            }
        }
    }

    @Override
    void init(Object... value) {
        uiComponents = vaadin.uiComponents
        pid = uiComponents."${subkeyPrefix + PID}"
        pname = uiComponents."${subkeyPrefix + PNAME}"
        pbudget = uiComponents."${subkeyPrefix + PBUDGET}"
        newButton = uiComponents."${subkeyPrefix}newbutton"
        editButton = uiComponents."${subkeyPrefix}editbutton"
        saveButton = uiComponents."${subkeyPrefix}savebutton"
        cancelButton = uiComponents."${subkeyPrefix}cancelbutton"
        projectTree.selectionModel.addListenerForKey(this, 'Project')
        projectTree.selectionModel.addRootChangeListener(this)
        // build state machine
        sm = new TabViewStateMachine(TabViewStateMachine.State.TOPTAB, 'PrjTab')
        configureSm()
        sm.execute(Event.Init)
    }

    @Override
    void onItemSelected(Map<String, Serializable> projectItemId) {
        currentItemId = projectItemId
        initItem((Long) projectItemId['id'])
        sm.execute(Event.Select, projectItemId['id'])
    }

    @Override
    void onRootChanged(Map<String, Serializable> projectItemId) {
        onItemSelected(projectItemId)
    }

    @Override
    protected getCurrentItemId() { currentItemId }

    @Override
    protected Long getCurrentDomainId() { (Long) currentItemId['id'] }

    @Override
    protected String getCurrentCaption() { currentDto.name }

    @Override
    protected getMatchForNewItem() {
        [type: ProjectTree.PROJECT_TYPE,
         id  : currentDto.id]
    }

    @Override
    protected createemptymode() {
//        authorizationService.roles
        def user = authorizationService.user
        if (authorizationService.hasRole('ROLE_ADMIN')) {
            super.createemptymode()
        } else {
            Notification.show("Sorry, you don't have the rights to do that.");
            newButton.enabled = true
            sm.currentState
        }
    }

    /** prepare EMPTY state */
    @Override
    protected emptymode() {
        clearFields()
        [pname, pbudget, saveButton, cancelButton, editButton]
                .each { it.enabled = false }
        newButton.enabled = true
    }

    /** prepare SHOW state */
    @Override
    protected showmode() {
        initItem(currentDto.id)
        [pname, pbudget, saveButton, cancelButton]
                .each { it.enabled = false }
        [editButton, newButton].each { it.enabled = true }
    }

    /** prepare for editing in EDIT, CREATE, CREATEEMPTY states */
    @Override
    protected editmode() {
        projectTree.onEditItem()
        [pname, pbudget, saveButton, cancelButton].each { it.enabled = true }
        [editButton, newButton].each { it.enabled = false }
    }

    /** clear all editable fields */
    @Override
    protected clearFields() {
        [pname, pbudget].each { it.clear() }
    }

    /**
     * for the given persistent object id, fetch the full dto
     * and save it in field currentDto
     * @param itemId object id
     */
    @Override
    protected void initItem(Long itemId) {
        currentDto = projectService.getProjectDetails((Long) itemId)
        setFieldValues()
    }

    /**
     * set all fields from the current full dto object
     */
    @Override
    protected void setFieldValues() {
        pid.value = currentDto.id.toString()
        pname.value = currentDto.name
        pbudget.value = currentDto.budget.toString()
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
        ProjectDto.CSet command = new ProjectDto.CSet()
        command.id = id
        command.name = pname.value
        command.budget = new BigDecimal(longFrom(pbudget.value))
        currentDto = projectService.createOrUpdateProject(command)
    }

}
