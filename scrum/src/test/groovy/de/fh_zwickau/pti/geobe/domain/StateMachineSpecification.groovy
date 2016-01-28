package de.fh_zwickau.pti.geobe.domain

import de.geobe.util.vaadin.DialogStateMachine
import groovy.util.logging.Slf4j
import spock.lang.Specification

/**
 * @author georg beier
 */
@Slf4j
class StateMachineSpecification extends Specification {

    private enum State {
        A, B, C, D
    }

    private enum Event {
        X, Y, Z
    }

    def 'the basic statemachine behaviour'() {
        setup: ' a state machine'
        DialogStateMachine<State, Event> sm = new DialogStateMachine<>(State.A)
        when: 'actions are added'
        sm.addTransition(State.A, State.B, Event.X, { log.info('A-X->B') })
        sm.addTransition(State.A, State.C, Event.Y, { log.info('A-Y->C') })
        sm.addTransition(State.A, State.A, Event.Z, { log.info('A-Z->A') })
        sm.addTransition(State.B, State.A, Event.Z, { log.info('B-Z->A') })
        sm.addTransition(State.C, State.A, Event.Z, { log.info('C-Z->A') })
        sm.addTransition(State.D, State.A, Event.Z, { log.info('D-Z->A') })
        sm.addTransition(State.B, State.D, Event.X, { log.info('B-X->D') })
        sm.addTransition(State.C, State.D, Event.X, { log.info('C-X->!D but B'); State.B })
        then: 'machine should work'
        assert sm.execute(Event.X) == State.B
        assert sm.execute(Event.X) == State.D
        assert sm.execute(Event.Z) == State.A
        assert sm.execute(Event.Y) == State.C
        assert sm.execute(Event.X) == State.B
        assert sm.execute(Event.Y) == State.B
    }

    def 'state machine executes closure with parameters'() {
        setup: ' a state machine and context variables'
        DialogStateMachine<State, Event> sm = new DialogStateMachine<>(State.A)
        def r1, r2
        sm.addTransition(State.A, State.B, Event.X, { p1 -> r1 = p1[0]; r2 = p1[1] })
        when: 'action with parameters is executes'
        sm.execute(Event.X, 'hurz', 42)
        then: 'parameters should be accessible for closure'
        assert r1 == 'hurz'
        assert r2 == 42


    }
}
