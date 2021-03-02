package edu.uno.advcomparch.statemachine;

import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

public class StateMachineListener extends StateMachineListenerAdapter<String, String> {

    @Override
    public void stateChanged(State from, State to) {
        System.out.printf("Transitioned from %s to %s%n", from == null ?
                "none" : from.getId(), to.getId());
    }
}