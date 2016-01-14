package de.geobe.util.vaadin

import groovy.util.logging.Slf4j

/**
 * A state machine for tree controlled tab views. There are two slightly
 * different types of tab views:
 * <ol><li> TOPTAB: tab view(s) representing objects on the top level of the tree view have their
 * create button active even if no top level element is selected directly or indirectly via a subelement</li>
 * <li> SUBTAB: tab views representing objects below the top level have their create button only active when an
 * object of a higher level is directly or indirectly selected so that the newly created object can be attached
 * to this higher level ("parent") element.</li></ol>
 * <p>The behaviour model assumes that the root cannot change during edit or create, so the selection component
 * (Tree) should be disabled during edit or create.</p>
 * <code>
 *
 *            SUBTAB             TOPTAB
 *               O                  O
 *               |                  |
 *             <init>             <init>
 *               |                  |
 *           +---v---+           +--v------+
 *           | INIT  |---root--->|  EMPTY  |
 *           +-v-----+           +|-^-v--^-+
 *             |                  | | |  |
 *             |    +---select----+ | |  |
 *        select    |  +----root----+ |  |
 *             |    |  |              |  |
 *     +----+  |    |  |         create cancel
 *    select|  |    |  |              |  |
 *     |  +-v--v----v--^-+       +----v--^-----+
 *     +-<|   SHOW       |<-save-| CREATEEMPTY |
 *        +v--^--^-v---v-+       +-------------+
 *         |  |  | |   +-------------------------+
 *         |  |  +-|-------cancel------+---------|------+
 *         |  +--|-|-------save----+---|---------|---+  |
 *         |  |  | +-------------+  |  |         |   |  |
 *         |  |  |               |  |  |         |   |  |
 *    create  |  |            edit  |  |    dialog   |  |
 *         |  |  |               |  |  |         |   |  |
 *        +v--^--^--+          +-v--^--^-+     +-v---^--^-+
 *        |  CREATE |          |  EDIT   |     |  DIALOG  |
 *        +---------+          +---------+     +----------+
 *
 * </code>
 *  Created by georg beier on 04.12.2015.
 */
@Slf4j
class TabViewStateMachine {

    public static enum State {
        SUBTAB,         // creation state for tab views of sublevel objects
        TOPTAB,         // creation state for tab views of toplevel objects
        INIT,           // nothing is selected in the controlling tree
        EMPTY,          // no object is selected for this tab, but a root node is selected
        SHOW,           // an object is selected and shown on the tab
        CREATEEMPTY,    // starting from EMPTY (important for Cancel events!), a new Object is created
        CREATE,         // starting from SHOW (important for Cancel events!), a new Object is created
        EDIT,           // selected object is being edited
        DIALOG,         // we are in a modal dialog
    }

    public static enum Event {
        Init,     // initialise state machine
        Select,   // an item of the displayed class was selected
        Root,     // a new branch was selected, either by selecting another top level object or some subobject
        Edit,     // start editing the selected object
        Create,   // start creating a new object
        Cancel,   // cancel edit or create
        Save,     // save newly edited or created object
        Dialog,   // enter a modal dialog
    }

    private DialogStateMachine sm

    private Map<State, Closure> onEntry = new HashMap<>()
    private Map<State, Closure> onExit = new HashMap<>()
    private Map<Integer, Closure> onTransition = new HashMap<>()

    public Map<State, Closure> getOnEntry() { onEntry }

    public Map<State, Closure> getOnExit() { onExit }

    public Map<Integer, Closure> getOnTransition() { onTransition }

    State getCurrentState() { (State) sm?.currentState }


    TabViewStateMachine(State initial, String id = 'tvsm') {
        switch (initial) {
            case State.TOPTAB:
                sm = new DialogStateMachine(State.TOPTAB, id)
                break
            case State.SUBTAB:
                sm = new DialogStateMachine(State.SUBTAB, id)
                break
            default:
                log.warn("unexpected initial state $initial")
                sm = new DialogStateMachine(State.TOPTAB, id)
        }
        buildDialogSM()
    }

    public execute(Event event, Serializable... params) {
        sm.execute(event, params)
    }

    /**
     * delegate calculating a unique transition index from current state
     * and triggering event to DialogStateMachine
     * @param st current state
     * @param ev event triggering transition
     * @return a unique Integer computed from state and event
     */
    public Integer trix(State st, Event ev) {
        sm.trix(st, ev)
    }

    void addTransition(State from, State to, Event ev) {
        sm.addTransition(from, to, ev) {
            onExit[(from)]?.call()
            onTransition[trix(from, ev)]?.call()
            onEntry[(to)]?.call()
        }
    }

    /**
     * Build a state machine to control dialog behaviour
     */
    private void buildDialogSM() {
        addTransition(State.SUBTAB, State.INIT, Event.Init)
        addTransition(State.INIT, State.SHOW, Event.Select)
        addTransition(State.INIT, State.EMPTY, Event.Root)
        addTransition(State.TOPTAB, State.EMPTY, Event.Init)
        addTransition(State.EMPTY, State.EMPTY, Event.Root)
        addTransition(State.EMPTY, State.CREATEEMPTY, Event.Create)
        addTransition(State.EMPTY, State.SHOW, Event.Select)
        addTransition(State.CREATEEMPTY, State.EMPTY, Event.Cancel)
        addTransition(State.CREATEEMPTY, State.SHOW, Event.Save)
        addTransition(State.SHOW, State.EDIT, Event.Edit)
        addTransition(State.SHOW, State.CREATE, Event.Create)
        addTransition(State.SHOW, State.SHOW, Event.Select)
        addTransition(State.SHOW, State.EMPTY, Event.Root)
        addTransition(State.EDIT, State.SHOW, Event.Save)
        addTransition(State.EDIT, State.SHOW, Event.Cancel)
        addTransition(State.CREATE, State.SHOW, Event.Save)
        addTransition(State.CREATE, State.SHOW, Event.Cancel)
        addTransition(State.SHOW, State.DIALOG, Event.Dialog)
        addTransition(State.DIALOG, State.SHOW, Event.Save)
        addTransition(State.DIALOG, State.SHOW, Event.Cancel)
    }
}
