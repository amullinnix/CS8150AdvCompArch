package edu.uno.advcomparch.state_machine;

import edu.uno.advcomparch.AbstractCompArchTest;
import edu.uno.advcomparch.controller.Level1Controller;
import edu.uno.advcomparch.controller.Level2Controller;
import edu.uno.advcomparch.cpu.DefaultCPU;
import edu.uno.advcomparch.repository.DataRepository;
import edu.uno.advcomparch.repository.DataResponse;
import edu.uno.advcomparch.repository.DataResponseType;
import edu.uno.advcomparch.statemachine.L1ControllerState;
import edu.uno.advcomparch.statemachine.L1InMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;

import java.util.LinkedList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

public class StateMachineTests extends AbstractCompArchTest {

    @Autowired
    private Level1Controller level1Controller;

    @Autowired
    private Level2Controller level2Controller;

    @Autowired
    DataRepository<String, String> l1DataRepository;

    @Autowired
    DefaultCPU cpu;

    @Autowired
    private StateMachine<L1ControllerState, L1InMessage> stateMachine;

    @BeforeEach
    public void beforeEach() {
        var l1Queue = new LinkedList<String>();
        Mockito.when(level1Controller.getQueue()).thenReturn(l1Queue);
        Mockito.when(l1DataRepository.get(any(String.class))).thenReturn(new DataResponse(DataResponseType.HIT, "data"));

        level2Controller.getQueue().clear();

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
    public void testExtendedStateMessage() {
        stateMachine.getExtendedState().getVariables().put("data", "blargh");
        stateMachine.sendEvent(L1InMessage.START);
        assertThat(stateMachine.getState().getId()).isEqualTo(L1ControllerState.HIT);
        stateMachine.sendEvent(L1InMessage.CPUREAD);
        assertThat(stateMachine.getState().getId()).isEqualTo(L1ControllerState.RDWAITD);
    }

    @Test
    @Disabled
    public void testL1DHit() {
        stateMachine.sendEvent(L1InMessage.START);

        org.springframework.messaging.Message<L1InMessage> cpuReadMessage = MessageBuilder
        .withPayload(L1InMessage.CPUREAD)
        .setHeader("source", "cpu")
        .setHeader("data", "data")
        .build();

        stateMachine.sendEvent(cpuReadMessage);
        assertThat(stateMachine.getState().getId()).isEqualTo(L1ControllerState.HIT);

        Mockito.verify(cpu, Mockito.times(1)).data(any());
    }

    @Test
    @Disabled
    public void testL1DMISS() {
        stateMachine.sendEvent(L1InMessage.START);

        Mockito.when(l1DataRepository.get(any(String.class))).thenReturn(new DataResponse(DataResponseType.MISSC, "data"));

        org.springframework.messaging.Message<L1InMessage> cpuReadMessage = MessageBuilder
                .withPayload(L1InMessage.CPUREAD)
                .setHeader("source", "cpu")
                .setHeader("data", "data")
                .build();

        stateMachine.sendEvent(cpuReadMessage);
        assertThat(stateMachine.getState().getId()).isEqualTo(L1ControllerState.RDWAITD);
        assertThat(stateMachine.getState().getId()).isEqualTo(L1ControllerState.RDL2WAITD);
    }

    @Test
    public void testL1HitWithPlan() throws Exception {
        StateMachineTestPlanBuilder.<L1ControllerState, L1InMessage>builder()
                .stateMachine(stateMachine)
                .step()
                .sendEvent(L1InMessage.START)
                .expectStates(L1ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(L1InMessage.CPUREAD)
                        .setHeader("source", "cpu")
                        .setHeader("data", "someData")
                        .build())
                .expectStateChanged(2)
                .expectStates(L1ControllerState.HIT)
                .and()
                .build()
                .test();

        Mockito.verify(cpu, Mockito.times(2)).data(any());
    }

    @Test
    public void testL1MissCWithPlan() throws Exception {
        Mockito.when(l1DataRepository.get(any(String.class))).thenReturn(new DataResponse(DataResponseType.MISSC, "data"));

        StateMachineTestPlanBuilder.<L1ControllerState, L1InMessage>builder()
                .stateMachine(stateMachine)
                .step()
                .sendEvent(L1InMessage.START)
                .expectStates(L1ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(L1InMessage.CPUREAD)
                        .setHeader("source", "cpu")
                        .setHeader("data", "someData")
                        .build())
                .expectStateChanged(3)
                .expectStates(L1ControllerState.RDL2WAITD)
                .and()
                .build()
                .test();

        // If we've missed then enqueue
        assertThat(level2Controller.getQueue()).hasSize(1);
    }

    @Test
    public void testL1MissIWithPlan() throws Exception {
        Mockito.when(l1DataRepository.get(any(String.class))).thenReturn(new DataResponse(DataResponseType.MISSI, "data"));

        StateMachineTestPlanBuilder.<L1ControllerState, L1InMessage>builder()
                .stateMachine(stateMachine)
                .step()
                .sendEvent(L1InMessage.START)
                .expectStates(L1ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(L1InMessage.CPUREAD)
                        .setHeader("source", "cpu")
                        .setHeader("data", "someData")
                        .build())
                .expectStateChanged(3)
                .expectStates(L1ControllerState.RDL2WAITD)
                .and()
                .build()
                .test();

        // If we've missed then enqueue
        assertThat(level2Controller.getQueue()).hasSize(1);
    }

    @Test
    public void testL1MissDWithPlan() throws Exception {
        Mockito.when(l1DataRepository.get(any(String.class))).thenReturn(new DataResponse(DataResponseType.MISSD, "data"));

        StateMachineTestPlanBuilder.<L1ControllerState, L1InMessage>builder()
                .stateMachine(stateMachine)
                .step()
                .sendEvent(L1InMessage.START)
                .expectStates(L1ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(L1InMessage.CPUREAD)
                        .setHeader("source", "cpu")
                        .setHeader("data", "someData")
                        .build())
                .expectStateChanged(3)
                .expectStates(L1ControllerState.RD2WAITD)
                .and()
                .build()
                .test();

        // If we've missed then enqueue
        assertThat(level2Controller.getQueue()).hasSize(1);
    }

    @Test
    public void testL2CPUReadWithPlanScenario2() throws Exception {
        Mockito.when(l1DataRepository.get(any(String.class))).thenReturn(new DataResponse(DataResponseType.MISSC, "data"));

        StateMachineTestPlanBuilder.<L1ControllerState, L1InMessage>builder()
                .stateMachine(stateMachine)
                .step()
                .sendEvent(L1InMessage.START)
                .expectStates(L1ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(L1InMessage.CPUREAD)
                        .setHeader("source", "cpu")
                        .setHeader("data", "someData")
                        .build())
                .expectStateChanged(3)
                .expectStates(L1ControllerState.RDL2WAITD)
                .and()
                .step()
                // L2 Sends Data Back
                .sendEvent(MessageBuilder
                        .withPayload(L1InMessage.DATA)
                        .setHeader("source", "L2")
                        .setHeader("data", new DataResponse(DataResponseType.HIT, "data"))
                        .build())
                .expectStateChanged(1)
                .expectStates(L1ControllerState.HIT)
                .and()
                .build()
                .test();

        assertThat(level2Controller.getQueue()).hasSize(1);
        Mockito.verify(cpu, Mockito.times(1)).data(any());
        Mockito.verify(l1DataRepository, Mockito.times(1)).write(any());
    }

    @Test
    public void testL2CPUReadWithPlanScenario4() throws Exception {
        Mockito.when(l1DataRepository.get(any(String.class))).thenReturn(new DataResponse(DataResponseType.MISSD, "data"));
        Mockito.when(l1DataRepository.victimize(any(String.class))).thenReturn(new DataResponse(DataResponseType.HIT, "data"));

        StateMachineTestPlanBuilder.<L1ControllerState, L1InMessage>builder()
                .stateMachine(stateMachine)
                .step()
                .sendEvent(L1InMessage.START)
                .expectStates(L1ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(L1InMessage.CPUREAD)
                        .setHeader("source", "cpu")
                        .setHeader("data", "someData")
                        .build())
                .expectStateChanged(3)
                .expectStates(L1ControllerState.RD2WAITD)
                .and()
                .step()
                // L2 Sends Data Back
                .sendEvent(MessageBuilder
                        .withPayload(L1InMessage.DATA)
                        .setHeader("source", "L2")
                        .setHeader("data", new DataResponse(DataResponseType.HIT, "data"))
                        .build())
                .expectStateChanged(1)
                .expectStates(L1ControllerState.RD1WAITD)
                .and()
                .build()
                .test();

        assertThat(level2Controller.getQueue()).hasSize(1);
        Mockito.verify(cpu, Mockito.times(1)).data(any());
        Mockito.verify(l1DataRepository, Mockito.times(1)).victimize(any());
        Mockito.verify(l1DataRepository, Mockito.times(1)).write(any());
        Mockito.verify(level2Controller, Mockito.times(1)).setData(any());
    }

    @Test
    public void testCpuWriteScenario1() throws Exception {
        StateMachineTestPlanBuilder.<L1ControllerState, L1InMessage>builder()
                .stateMachine(stateMachine)
                .step()
                .sendEvent(L1InMessage.START)
                .expectStates(L1ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(L1InMessage.CPUWRITE)
                        .setHeader("source", "cpu")
                        .setHeader("data", "someData")
                        .build())
                .expectStateChanged(1)
                .expectStates(L1ControllerState.HIT)
                .and()
                .build()
                .test();

        Mockito.verify(l1DataRepository, Mockito.times(1)).write(any());
    }

    @Test
    public void testCpuWriteScenario2() throws Exception {
        Mockito.when(l1DataRepository.get(any(String.class))).thenReturn(new DataResponse(DataResponseType.MISSI, "data"));

        StateMachineTestPlanBuilder.<L1ControllerState, L1InMessage>builder()
                .stateMachine(stateMachine)
                .step()
                .sendEvent(L1InMessage.START)
                .expectStates(L1ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(L1InMessage.CPUWRITE)
                        .setHeader("source", "cpu")
                        .setHeader("data", "someData")
                        .build())
                .expectStateChanged(1)
                .expectStates(L1ControllerState.HIT)
                .and()
                .step()
                // L2 Sends Data Back
                .sendEvent(MessageBuilder
                        .withPayload(L1InMessage.DATA)
                        .setHeader("source", "L2")
                        .setHeader("data", new DataResponse(DataResponseType.HIT, "data"))
                        .build())
                .expectStateChanged(1)
                .expectStates(L1ControllerState.RD1WAITD)
                .and()
                .build()
                .test();

        Mockito.verify(l1DataRepository, Mockito.times(1)).write(any());
    }

    @Test
    public void testCpuWriteScenario3() throws Exception {
        Mockito.when(l1DataRepository.get(any(String.class))).thenReturn(new DataResponse(DataResponseType.MISSC, "data"));

        StateMachineTestPlanBuilder.<L1ControllerState, L1InMessage>builder()
                .stateMachine(stateMachine)
                .step()
                .sendEvent(L1InMessage.START)
                .expectStates(L1ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(L1InMessage.CPUWRITE)
                        .setHeader("source", "cpu")
                        .setHeader("data", "someData")
                        .build())
                .expectStateChanged(1)
                .expectStates(L1ControllerState.HIT)
                .and()
                .step()
                // L2 Sends Data Back
                .sendEvent(MessageBuilder
                        .withPayload(L1InMessage.DATA)
                        .setHeader("source", "L2")
                        .setHeader("data", new DataResponse(DataResponseType.HIT, "data"))
                        .build())
                .expectStateChanged(1)
                .expectStates(L1ControllerState.RD1WAITD)
                .and()
                .build()
                .test();

        Mockito.verify(l1DataRepository, Mockito.times(1)).write(any());
    }

    @Test
    public void testCpuWriteScenario4() {
        // Gonna take a second to digest this one
    }
}
