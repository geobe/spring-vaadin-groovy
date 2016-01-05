package de.fh_zwickau.pti.geobe.view

import com.vaadin.event.ShortcutAction
import com.vaadin.spring.annotation.SpringComponent
import com.vaadin.spring.annotation.UIScope
import com.vaadin.ui.*
import com.vaadin.ui.themes.Reindeer
import de.fh_zwickau.pti.geobe.dto.TaskDto
import de.fh_zwickau.pti.geobe.service.TaskService
import de.fh_zwickau.pti.geobe.util.view.VaadinSelectionListener
import de.fh_zwickau.pti.geobe.util.view.VaadinTreeRootChangeListener
import de.geobe.util.vaadin.TabViewStateMachine
import de.geobe.util.vaadin.VaadinBuilder
import org.springframework.beans.factory.annotation.Autowired

import static TabViewStateMachine.Event
import static VaadinBuilder.C
import static VaadinBuilder.F

/**
 * editor tab for tasks
 *
 * Created by georg beier on 16.11.2015.
 */

@SpringComponent
@UIScope
class TaskTab extends TabBase
        implements VaadinSelectionListener, VaadinTreeRootChangeListener, Serializable {

    public static final String TAG = 'tag'
    public static final String IS_SUPERTASK = 'isSupertask'
    public static final String IS_COMPLETED = 'isCompleted'
    public static final String ESTIMATE = 'estimate'
    public static final String SPENT = 'spent'
    public static final String DESCRIPTION = 'description'
    private TextField tag, estimate, spent
    private TextArea description
    private CheckBox supertask, completed
    private Button newButton, editButton, saveButton, cancelButton, subtaskButton
    private Map<String, Serializable> currentItemId
    private Map<String, Serializable> currentTopItemId
    private TaskDto.QFull currentDto
    private UI ui
    private Component topComponent
    private SubtaskDialog dialog = new SubtaskDialog()

    @Autowired
    private TaskService taskService
    @Autowired
    private ProjectTree projectTree


    @Override
    Component build() {
        topComponent = vaadin."$C.vlayout"('Backlog', [spacing: true, margin: true]) {
            "$F.text"('Aufgabe', [uikey: TAG])
            "$C.hlayout"('Status', [spacing: true, margin: false]) {
                "$F.checkbox"('übergeordnet', [uikey: IS_SUPERTASK])
                "$F.checkbox"('abgeschlossen', [uikey: IS_COMPLETED])
            }
            "$F.text"('Schätzung', [uikey: ESTIMATE])
            "$F.text"('aktueller Verbrauch', [uikey: SPENT])
            "$F.textarea"('Beschreibung', [uikey: DESCRIPTION])
            "$C.hlayout"([uikey: 'buttonfield', spacing: true, gridPosition: [0, 3, 1, 3]]) {
                "$F.button"('New', [uikey        : 'newbutton', disableOnClick: true,
                                    clickListener: { sm.execute(Event.Create) }])
                "$F.button"('Edit', [uikey        : 'editbutton', disableOnClick: true,
                                     clickListener: { sm.execute(Event.Edit) }])
                "$F.button"('Subtask', [uikey        : 'subtaskbutton', disableOnClick: true, enabled: false,
                                        clickListener: { sm.execute(Event.Dialog) }])
                "$F.button"('Cancel', [uikey        : 'cancelbutton', disableOnClick: true, enabled: false,
                                       clickListener: { sm.execute(Event.Cancel) }])
                "$F.button"('Save', [uikey        : 'savebutton', disableOnClick: true, enabled: false,
                                     clickShortcut: ShortcutAction.KeyCode.ENTER,
                                     styleName    : Reindeer.BUTTON_DEFAULT,
                                     clickListener: { sm.execute(Event.Save) }])
            }
        }
        topComponent
    }

    @Override
    void init(Object... value) {
        uiComponents = vaadin.uiComponents
        tag = uiComponents."$subkeyPrefix$TAG"
        estimate = uiComponents."$subkeyPrefix$ESTIMATE"
        spent = uiComponents."$subkeyPrefix$SPENT"
        description = uiComponents."$subkeyPrefix$DESCRIPTION"
        completed = uiComponents."$subkeyPrefix$IS_COMPLETED"
        supertask = uiComponents."$subkeyPrefix$IS_SUPERTASK"
        newButton = uiComponents."${subkeyPrefix}newbutton"
        editButton = uiComponents."${subkeyPrefix}editbutton"
        saveButton = uiComponents."${subkeyPrefix}savebutton"
        cancelButton = uiComponents."${subkeyPrefix}cancelbutton"
        subtaskButton = uiComponents."${subkeyPrefix}subtaskbutton"
        projectTree.selectionModel.addListenerForKey(this, 'Task')
        projectTree.selectionModel.addRootChangeListener(this)
        // find the top level Vaadin Window
        ui = getVaadinUi(topComponent)
        // build dialog window
        dialog.build()
        // build state machine
        sm = new TabViewStateMachine(TabViewStateMachine.State.SUBTAB)
        configureSm()
        sm.smId = 'TskTab'
        sm.execute(Event.Init)
    }

    @Override
    void onItemSelected(Map<String, Serializable> taskItemId) {
        currentItemId = taskItemId
        initItem((Long) currentItemId['id'])
        sm.execute(Event.Select)
    }

    @Override
    void onRootChanged(Map<String, Serializable> projectItemId) {
        currentTopItemId = projectItemId
        sm.execute(Event.Root)
    }

    @Override
    protected getCurrentItemId() { currentItemId }

    @Override
    protected Long getCurrentDomainId() { (Long) currentItemId['id'] }

    @Override
    protected String getCurrentCaption() { currentDto.tag }

    @Override
    protected getMatchForNewItem() { [type: ProjectTree.TASK_TYPE, id: currentDto.id] }

    /** prepare INIT state */
    @Override
    protected initmode() {
        [tag, estimate, spent, description, completed, supertask,
         saveButton, cancelButton, subtaskButton, editButton, newButton].each { it.enabled = false }
    }
    /** prepare EMPTY state */
    @Override
    protected emptymode() {
        clearFields()
        currentDto = null
        [tag, estimate, spent, description, completed, supertask,
         saveButton, cancelButton, editButton, subtaskButton].each {
            it.enabled = false
        }
        [newButton].each { it.enabled = true }
    }
    /** prepare SHOW state */
    @Override
    protected showmode() {
        [tag, estimate, spent, description, completed, supertask, saveButton, cancelButton].each {
            it.enabled = false
        }
        [editButton, newButton, subtaskButton].each { it.enabled = true }
    }

    @Override
    protected createemptymode() {
        createmode()
    }

    @Override
    protected createmode() {
        projectTree.onEditItem()
        [tag, estimate, spent, description, supertask, completed, saveButton, cancelButton].each { it.enabled = true }
        [editButton, newButton, subtaskButton].each { it.enabled = false }
//        saveButton.setClickShortcut(ShortcutAction.KeyCode.ENTER)
    }
    /** prepare for editing in EDITstates */
    @Override
    protected editmode() {
        projectTree.onEditItem()
        if (currentDto.classname == 'Subtask')
            completed.enabled = true
        [tag, estimate, spent, description, saveButton, cancelButton].each { it.enabled = true }
        [editButton, newButton, subtaskButton].each { it.enabled = false }
//        saveButton.setClickShortcut(ShortcutAction.KeyCode.ENTER)
    }
    /** prepare for working in DIALOG state */
    protected dialogmode() {
        projectTree.onEditItem()
        [dialog.tag, dialog.estimate, dialog.spent, dialog.description,
         dialog.completed, dialog.supertask].each { it.clear() }
        [dialog.saveButton, dialog.cancelButton].each { it.enabled = true }
        ui.addWindow(dialog.window)
    }
    /** leaving DIALOG state with save */
    protected saveDialog() {
        createSubtask()
        dialog.window.close()
        projectTree.onEditItemDone(currentItemId, currentCaption, true)
    }
    /** leaving DIALOG state with cancel */
    protected cancelDialog() {
        dialog.window.close()
        projectTree.onEditItemDone(currentItemId, currentCaption)
    }
    /** clear all editable fields */
    @Override
    protected clearFields() {
        [tag, estimate, spent, description, completed, supertask].each { it.clear() }
    }
    /**
     * for the given persistent object id, fetch the full dto and save it in field currentDto
     * @param itemId object id
     */
    @Override
    protected void initItem(Long taskId) {
        currentDto = taskService.getTaskDetails(taskId)
        setFieldValues()
    }
    /**
     * set all fields from the current full dto object
     */
    @Override
    protected void setFieldValues() {
        tag.value = currentDto.tag
        estimate.value = currentDto.estimate.toString()
        spent.value = currentDto.spent.toString()
        description.value = currentDto.description
        completed.value = currentDto.completed
        supertask.value = currentDto.classname == 'CompoundTask'
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
        TaskDto.CSet command = new TaskDto.CSet()
        command.id = id
        command.tag = tag.value
        command.estimate = longFrom(estimate.value)
        command.spent = longFrom(spent.value)
        command.description = description.value
        command.completed = completed.value
        command.classname = supertask.value ? 'CompoundTask' : 'Subtask'
        // determine level for a new item
        if (id == 0) {
            if (!currentDto || currentDto.project.all) {
                // we are on top level of tasks
                command.projectId = (Long) currentTopItemId['id']
            } else {
                // we are on a lower level
                command.supertaskId = currentDto.supertask.firstId
            }
        }
        currentDto = taskService.createOrUpdate(command)
    }

    def createSubtask() {
        TaskDto.CSet command = new TaskDto.CSet()
        command.id = 0
        command.supertaskId = currentDto.id
        command.tag = dialog.tag.value
        command.estimate = longFrom(dialog.estimate.value)
        command.spent = longFrom(dialog.spent.value)
        command.description = dialog.description.value
        command.completed = dialog.completed.value
        command.classname = dialog.supertask.value ? 'CompoundTask' : 'Subtask'
//        command.projectId = (Long) currentTopItemId['id']
        def newNode = taskService.createSubtask(command)
        newNode
    }

    private class SubtaskDialog {
        TextField tag, estimate, spent
        TextArea description
        CheckBox supertask, completed
        Button saveButton, cancelButton
        Window window

        private VaadinBuilder winBuilder = new VaadinBuilder()

        public Window build() {
            String keyPrefix = "${subkeyPrefix}dialog."
            winBuilder.keyPrefix = keyPrefix
            window = winBuilder."$C.window"('Subtask anlegen', [spacing: true, margin: true,
                                                                modal  : true, closable: false]) {
                "$C.vlayout"('top', [spacing: true, margin: true]) {
                    "$F.text"('Aufgabe', [uikey: TAG])
                    "$C.hlayout"('Status', [spacing: true, margin: false]) {
                        "$F.checkbox"('übergeordnet', [uikey: IS_SUPERTASK])
                        "$F.checkbox"('abgeschlossen', [uikey: IS_COMPLETED])
                    }
                    "$F.text"('Schätzung', [uikey: ESTIMATE])
                    "$F.text"('aktueller Verbrauch', [uikey: SPENT])
                    "$F.textarea"('Beschreibung', [uikey: DESCRIPTION])
                    "$C.hlayout"([uikey: 'buttonfield', spacing: true]) {
                        "$F.button"('Cancel', [uikey        : 'cancelbutton', disableOnClick: true, enabled: true,
                                               clickListener: { sm.execute(Event.Cancel) }])
                        "$F.button"('Save', [uikey        : 'savebutton', disableOnClick: true, enabled: true,
                                             clickListener: { sm.execute(Event.Save) }])
                    }
                }
            }

            def dialogComponents = winBuilder.uiComponents
            tag = dialogComponents."$keyPrefix$TAG"
            estimate = dialogComponents."$keyPrefix$ESTIMATE"
            spent = dialogComponents."$keyPrefix$SPENT"
            description = dialogComponents."$keyPrefix$DESCRIPTION"
            completed = dialogComponents."$keyPrefix$IS_COMPLETED"
            supertask = dialogComponents."$keyPrefix$IS_SUPERTASK"
            saveButton = dialogComponents."${keyPrefix}savebutton"
            cancelButton = dialogComponents."${keyPrefix}cancelbutton"
            window.center()
//            window.modal = true
//            window.setClosable(false)
        }
    }

}
