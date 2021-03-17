package edu.uno.advcomparch.state_machine;

import edu.uno.advcomparch.AbstractCompArchTest;
import edu.uno.advcomparch.statemachine.L1ControllerState;
import edu.uno.advcomparch.statemachine.L1InMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;

import static org.assertj.core.api.Assertions.assertThat;

public class StateMachineTests extends AbstractCompArchTest {

    @Autowired
    private StateMachine<L1ControllerState, L1InMessage> stateMachine;

    @BeforeEach
    public void beforeEach() {
        stateMachine.start();
    }

    @AfterEach
    public void afterEach() {
        stateMachine.stop();
    }

    @Test
    public void testStartState() {
        assertThat(stateMachine.getState().getId()).isEqualTo(L1ControllerState.START);
    }

    @Test
    public void testStartEvent() {
        stateMachine.sendEvent(L1InMessage.START);
        assertThat(stateMachine.getState().getId()).isEqualTo(L1ControllerState.HIT);
    }

    @Test
    public void testCPURead() {
        stateMachine.sendEvent(L1InMessage.START);
        stateMachine.sendEvent(L1InMessage.CPUREAD);
        assertThat(stateMachine.getState().getId()).isEqualTo(L1ControllerState.RDWAITD);
    }

}
