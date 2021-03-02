package edu.uno.advcomparch.statemachine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;

public class StateMachineRunner {

    @Autowired
    private StateMachine<String, String> stateMachine;

    public void runStateMachine() {
        stateMachine.start();
        stateMachine.sendEvent("E1");
        stateMachine.getState();
    }
}
