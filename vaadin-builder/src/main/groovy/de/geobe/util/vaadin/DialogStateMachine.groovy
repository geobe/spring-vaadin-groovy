package de.geobe.util.vaadin

import groovy.util.logging.Slf4j

/**
 * Implementation of a simple Moore state machine
 * Created by georg beier on 01.12.2015.
 */
@Slf4j
class DialogStateMachine<S extends Enum, E extends Enum> {

    private Map<S, Map<E, Closure>> stateMachine = new HashMap<>()
    private Map<S, Map<E, S>> nextState = new HashMap<>()
    private S currentState
    private String smid

    def getCurrentState() { currentState }

    def setSmId(String id) { smid = id }

    /**
     * create instance with initial state
     * @param start
     * @param id
     */
    DialogStateMachine(S start, String id = 'default') {
        currentState = start
        smid = id
    }

    /**
     * add an action to this state machine
     * @param state the current state
     * @param nextState the state to go after executing the action. Can be overwritten by the action
     *                  if it is returning an Enum<S> value
     * @param event event that triggers the state machine
     * @param action closure that is executed for combination of state and event
     */
    void addAction(S state, S next, E event, Closure action) {
        Map<E, Closure> actions
        Map<E, S> nextStates
        if (stateMachine[state]) {
            actions = stateMachine[state]
            nextStates = nextState[state]
        } else {
            actions = new HashMap<>()
            stateMachine[state] = actions
            nextStates = new HashMap<>()
            nextState[state] = nextStates
        }
        actions[event] = action
        if (next)
            nextStates[event] = next
    }

    /**
     * execute closure that is identified by currentState and event. After execution, statemachine will be
     * <ul><li>either in the following state as defined in addAction method, if closure returns no object of type S</li>
     * <li>or in the state returned by the closure.</li></ul>
     * @param event triggering event
     * @param params optional parameter to closure. Caution, closure will receive an Object[] Array
     * @return the current state after execution
     */
    S execute(E event, Serializable... params) {
        if (stateMachine[currentState][event]) {
            Closure action = stateMachine[currentState][event]
            def result = action(params)
            def next
            if (result instanceof S) {
                next = result
            } else {
                next = (nextState[currentState][event] ?: currentState)
            }
            log.info("Transition $smid: $currentState--$event->$next")
            currentState = next
        } else {
            log.info("ignored event $event in state $currentState")
        }
        currentState
    }
}