package de.fh_zwickau.pti.geobe.view

import de.geobe.util.vaadin.SubTree
import de.geobe.util.vaadin.TabViewStateMachine
import org.springframework.beans.factory.annotation.Autowired

/**
 * implement basic methods for tab views
 * Created by georg beier on 09.12.2015.
 */
abstract class TabBase extends SubTree {
    protected TabViewStateMachine sm
    @Autowired
    protected ProjectTree projectTree

    protected configureSm() {
        sm.onEntry[TabViewStateMachine.State.INIT] = {
            clearFields()
            initmode()
        }
        sm.onEntry[TabViewStateMachine.State.EMPTY] = {
            emptymode()
        }
        sm.onEntry[TabViewStateMachine.State.SHOW] = {
            showmode()
        }
        sm.onEntry[TabViewStateMachine.State.CREATEEMPTY] = {
            createemptymode()
        }
        sm.onEntry[TabViewStateMachine.State.CREATE] = {
            clearFields()
            createmode()
        }
        sm.onEntry[TabViewStateMachine.State.EDIT] = {
            editmode()
        }
        sm.onEntry[TabViewStateMachine.State.DIALOG] = {
            dialogmode()
        }

        sm.onTransition[sm.trix(TabViewStateMachine.State.CREATEEMPTY, TabViewStateMachine.Event.Save)] = {
            saveItem(0)
            setFieldValues()
            projectTree.onEditItemDone(matchForNewItem, currentCaption, true)
        }
        sm.onTransition[sm.trix(TabViewStateMachine.State.CREATEEMPTY, TabViewStateMachine.Event.Cancel)] = {
            projectTree.onEditItemDone('', '')
        }
        sm.onTransition[sm.trix(TabViewStateMachine.State.EDIT, TabViewStateMachine.Event.Save)] = {
            saveItem(currentDomainId)
            projectTree.onEditItemDone(currentItemId,  currentCaption)
        }
        sm.onTransition[sm.trix(TabViewStateMachine.State.EDIT, TabViewStateMachine.Event.Cancel)] = {
            setFieldValues()
            projectTree.onEditItemDone(currentItemId,  currentCaption)
        }
        sm.onTransition[sm.trix(TabViewStateMachine.State.CREATE, TabViewStateMachine.Event.Save)] = {
            saveItem(0)
            projectTree.onEditItemDone(matchForNewItem,  currentCaption, true)
        }
        sm.onTransition[sm.trix(TabViewStateMachine.State.CREATE, TabViewStateMachine.Event.Cancel)] = {
            setFieldValues()
            projectTree.onEditItemDone(currentItemId,  currentCaption)
        }
        sm.onTransition[sm.trix(TabViewStateMachine.State.DIALOG, TabViewStateMachine.Event.Save)] = {
            saveDialog()
        }
        sm.onTransition[sm.trix(TabViewStateMachine.State.DIALOG, TabViewStateMachine.Event.Cancel)] = {
            cancelDialog()
        }
    }

    /** item id of currently selected object from vaadin selection component */
    protected abstract getCurrentItemId()
    /** value for the domain object id of currently displayed object */
    protected abstract Long getCurrentDomainId()
    /** get caption of current object for display in selection component */
    protected abstract String getCurrentCaption()
    /** item match mimics id for searching item in vaadin selection */
    protected abstract getMatchForNewItem()

    /** prepare for editing in CREATEEMPTY state */
    protected createemptymode() { editmode() }
    /** prepare for editing in CREATE state */
    protected createmode() { editmode() }
    /** prepare for working in DIALOG state */
    protected dialogmode() {}
    /** leaving DIALOG state with save */
    protected saveDialog() {}
    /** leaving DIALOG state with cancel */
    protected cancelDialog() {}
    /** prepare for editing in EDIT state */
    protected abstract editmode()
    /** prepare INIT state */
    protected initmode() {}
    /** prepare EMPTY state */
    protected abstract emptymode()
    /** prepare SHOW state */
    protected abstract showmode()
    /** clear all editable fields */
    protected abstract clearFields()
    /**
     * for the given persistent object id, fetch the full dto and save it in field currentDto
     * @param itemId object id
     */
    protected abstract void initItem(Long itemId)
    /**
     * set all fields from the current full dto object
     */
    protected abstract void setFieldValues()

    protected abstract saveItem(Long id)
}