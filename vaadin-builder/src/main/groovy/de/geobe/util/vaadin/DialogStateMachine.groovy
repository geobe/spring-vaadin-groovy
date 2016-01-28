package de.geobe.util.vaadin

import groovy.util.logging.Slf4j

/**
 * Implementation of a simple state machine
 * @author georg beier
 * @param S enumertion Type for States
 * @param E enumeration type for events
 */
@Slf4j
class DialogStateMachine<S extends Enum, E extends Enum> {
    /** map for actions, indexed by combination of currentState and event */
    private Map<Integer,Closure> stateMachine = new HashMap<>()
    /** map for next states, indexed by combination of currentState and event */
    private Map<Integer, S> nextState = new HashMap<>()
    /** store current state */
    private S currentState
    /** for logging info to identify state machine instance */
    private String smid

    def getCurrentState() { currentState }

    def setSmId(String id) { smid = id }

    /**
     * create instance with initial fromState
     * @param start initial fromState
     * @param id identifying string for logging and debug
     */
    DialogStateMachine(S start, String id = 'default') {
        currentState = start
        smid = id
    }

    /**
     * add an action to this fromState machine
     * @param fromState the current state
     * @param toState the state to go after executing the action.
     *        Can be overwritten by the action if it is returning an Enum<S> value
     * @param event event that triggers the fromState machine
     * @param action closure that is executed for combination of fromState and event
     */
    void addTransition(S fromState, S toState, E event, Closure action) {
        Integer index = trix(fromState, event)
        stateMachine[index] = action
        if(toState)
            nextState[index] = toState
    }

    /**
     * execute closure that is identified by currentState and event.
     * After execution, statemachine will be
     * <ul><li>in the following state as defined in addTransition method,
     * if closure returns no object of type S</li>
     * <li>in the state returned by the closure.</li>
     * <li>If no following state is defined, statemachine will stay in currentState.</li></ul>
     * @param event triggering event
     * @param params optional parameter to closure.
     *        Caution, closure will receive an Object[] Array
     * @return the current state after execution
     */
    S execute(E event, Serializable... params) {
        Integer index = trix(currentState, event)
        if (stateMachine[index]) {
            Closure action = stateMachine[index]
            def result = action(params)
            def next
            if (result instanceof S) {
                next = result
            } else {
                next = (nextState[index] ?: currentState)
            }
            log.info("Transition $smid: $currentState--$event->$next")
            currentState = next
        } else {
            log.info("ignored event $event in fromState $currentState")
        }
        currentState
    }

    /**
     * calculate a unique transition index from current state and triggering event
     * @param st current state
     * @param ev event triggering transition
     * @return a unique Integer computed from state and event
     */
    public Integer trix(S st, E ev) {
        def t = st.ordinal() + (ev.ordinal() << 12)
        t
    }
}