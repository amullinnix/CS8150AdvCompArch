package edu.uno.advcomparch.state_machine;

import edu.uno.advcomparch.AbstractCompArchTest;
import edu.uno.advcomparch.controller.Level1Controller;
import edu.uno.advcomparch.controller.Level2Controller;
import edu.uno.advcomparch.cpu.DefaultCPU;
import edu.uno.advcomparch.instruction.Message;
import edu.uno.advcomparch.repository.DataRepository;
import edu.uno.advcomparch.statemachine.L1ControllerState;
import edu.uno.advcomparch.statemachine.L1InMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;

import static org.assertj.core.api.Assertions.assertThat;

public class StateMachineTests extends AbstractCompArchTest {

    @Autowired
    private Level1Controller level1Controller;

    @Autowired
    private Level2Controller level2Controller;

    @Autowired
    private DataRepository<String, String> l1DataRepository;

    @Autowired
    DefaultCPU cpu;

    @Autowired
    private StateMachine<L1ControllerState, L1InMessage> stateMachine;

    @BeforeEach
    public void beforeEach() {
        stateMachine.start();
        level1Controller.getQueue().clear();
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

    @Test
    public void testExtendedStateMessage() {
        stateMachine.getExtendedState().getVariables().put("foo", new Message());
        stateMachine.sendEvent(L1InMessage.START);
        stateMachine.sendEvent(L1InMessage.CPUREAD);
        assertThat(stateMachine.getState().getId()).isEqualTo(L1ControllerState.RDWAITD);
        assertThat(level1Controller.getQueue()).hasSize(1);
    }

    @Test
    public void testLevel2ControllerMessage() {
        stateMachine.getExtendedState().getVariables().put("foo", new Message());
        stateMachine.sendEvent(L1InMessage.START);
        stateMachine.sendEvent(L1InMessage.CPUREAD);
        assertThat(stateMachine.getState().getId()).isEqualTo(L1ControllerState.RDWAITD);
        assertThat(level2Controller.getQueue()).hasSize(1);
    }

    @Test
    public void testMessage() {
        stateMachine.getExtendedState().getVariables().put("foo", new Message());
        stateMachine.sendEvent(L1InMessage.START);

        org.springframework.messaging.Message<L1InMessage> message = MessageBuilder
        .withPayload(L1InMessage.CPUREAD)
        .setHeader("source", "cpu")
        .build();

        stateMachine.sendEvent(message);

        org.springframework.messaging.Message<L1InMessage> message2 = MessageBuilder
                .withPayload(L1InMessage.DATA)
                .setHeader("source", "L1D")
                .build();

        stateMachine.sendEvent(message2);

        assertThat(stateMachine.getState().getId()).isEqualTo(L1ControllerState.RDWAITD);
        assertThat(level2Controller.getQueue()).hasSize(1);
    }
}
