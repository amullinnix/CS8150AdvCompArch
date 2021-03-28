package edu.uno.advcomparch.state_machine;

import edu.uno.advcomparch.AbstractCompArchTest;
import edu.uno.advcomparch.config.L2StateMachineTestConfiguration;
import edu.uno.advcomparch.controller.*;
import edu.uno.advcomparch.cpu.DefaultCPU;
import edu.uno.advcomparch.statemachine.L1ControllerState;
import edu.uno.advcomparch.statemachine.L1InMessage;
import edu.uno.advcomparch.statemachine.StateMachineMessageBus;
import edu.uno.advcomparch.storage.Level2DataStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.when;

@Disabled
@ContextConfiguration(classes = L2StateMachineTestConfiguration.class)
public class L2StateMachineTests extends AbstractCompArchTest {

    @Autowired
    Level2DataStore level2DataStore;

    @Autowired
    DefaultCPU cpu;

    @Autowired
    StateMachineMessageBus stateMachineMessageBus;

    @Autowired
    private StateMachine<L1ControllerState, L1InMessage> l2ControllerStateMachine;

    @BeforeEach
    public void beforeEach() {
        when(level2DataStore.canWriteToCache(any(Address.class)))
                .thenReturn(DataResponseType.HIT);
        when(level2DataStore.isDataPresentInCache(any(Address.class)))
                .thenReturn(DataResponseType.HIT);
        var cacheBlock = new CacheBlock(1,2);
        when(level2DataStore.getBlockAtAddress(any(Address.class)))
                .thenReturn(cacheBlock);

        stateMachineMessageBus.getL1MessageQueue().clear();

        l2ControllerStateMachine.start();
    }

    @AfterEach
    public void afterEach() {
        l2ControllerStateMachine.stop();
        Mockito.reset();
    }

    @Test
    public void testStartEvent() {
        assertThat(l2ControllerStateMachine.getState().getId()).isEqualTo(L1ControllerState.HIT);
    }

    @Test
    public void testExtendedStateMessage() {
        l2ControllerStateMachine.getExtendedState().getVariables().put("data", "blargh");
        assertThat(l2ControllerStateMachine.getState().getId()).isEqualTo(L1ControllerState.HIT);
        l2ControllerStateMachine.sendEvent(L1InMessage.CPUREAD);
        assertThat(l2ControllerStateMachine.getState().getId()).isEqualTo(L1ControllerState.RDWAITD);
    }

    @Test
    public void testL1HitCPUReadScenario1() throws Exception {
        StateMachineTestPlanBuilder.<L1ControllerState, L1InMessage>builder()
                .stateMachine(l2ControllerStateMachine)
                .step()
                .expectStates(L1ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(L1InMessage.CPUREAD)
                        .setHeader("source", "L1")
                        .setHeader("address", "1010101010")
                        .setHeader("bytes", 4)
                        .build())
                .expectStateChanged(2)
                .expectStates(L1ControllerState.HIT)
                .and()
                .build()
                .test();

        Mockito.verify(cpu, Mockito.atMostOnce()).data(any());
        Mockito.verify(level2DataStore, Mockito.atMostOnce()).isDataPresentInCache(any());
        Mockito.verify(level2DataStore, Mockito.atMostOnce()).getBlockAtAddress(any(Address.class));
    }

    @Test
    public void testL1MissCCpuReadScenario2() throws Exception {
        when(level2DataStore.isDataPresentInCache(any(Address.class)))
                .thenReturn(DataResponseType.MISSC);

        StateMachineTestPlanBuilder.<L1ControllerState, L1InMessage>builder()
                .stateMachine(l2ControllerStateMachine)
                .step()
                .expectStates(L1ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(L1InMessage.CPUREAD)
                        .setHeader("source", "cpu")
                        .setHeader("address", "1010101010")
                        .setHeader("bytes", 4)
                        .build())
                .expectStateChanged(3)
                .expectStates(L1ControllerState.RDL2WAITD)
                .and()
                .build()
                .test();
    }

    @Test
    public void testL1MissICpuReadScenario3() throws Exception {
        when(level2DataStore.isDataPresentInCache(any(Address.class)))
                .thenReturn(DataResponseType.MISSI);

        StateMachineTestPlanBuilder.<L1ControllerState, L1InMessage>builder()
                .stateMachine(l2ControllerStateMachine)
                .step()
                .expectStates(L1ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(L1InMessage.CPUREAD)
                        .setHeader("source", "cpu")
                        .setHeader("address", "1010101010")
                        .setHeader("bytes", 4)
                        .build())
                .expectStateChanged(3)
                .expectStates(L1ControllerState.RDL2WAITD)
                .and()
                .build()
                .test();
    }

    @Test
    public void testL1MissD() throws Exception {
        when(level2DataStore.isDataPresentInCache(any(Address.class)))
                .thenReturn(DataResponseType.MISSD);

        StateMachineTestPlanBuilder.<L1ControllerState, L1InMessage>builder()
                .stateMachine(l2ControllerStateMachine)
                .step()
                .expectStates(L1ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(L1InMessage.CPUREAD)
                        .setHeader("source", "cpu")
                        .setHeader("address", "1010101010")
                        .setHeader("bytes", 4)
                        .build())
                .expectStateChanged(3)
                .expectStates(L1ControllerState.RD2WAITD)
                .and()
                .build()
                .test();

        // TODO - IS MISSD behavior different?
//        assertThat(level2Controller.getQueue()).hasSize(1);
    }

    @Test
    public void testL2CPUReadScenario2() throws Exception {
        var data = new CacheBlock(1,2);
        when(level2DataStore.isDataPresentInCache(any(Address.class)))
                .thenReturn(DataResponseType.MISSI);

        StateMachineTestPlanBuilder.<L1ControllerState, L1InMessage>builder()
                .stateMachine(l2ControllerStateMachine)
                .step()
                .expectStates(L1ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(L1InMessage.CPUREAD)
                        .setHeader("source", "cpu")
                        .setHeader("address", "1010101010")
                        .setHeader("bytes", 4)
                        .build())
                .expectStateChanged(3)
                .expectStates(L1ControllerState.RDL2WAITD)
                .and()
                .step()
                // L2 Sends Data Back
                .sendEvent(MessageBuilder
                        .withPayload(L1InMessage.DATA)
                        .setHeader("source", "L2")
                        .setHeader("address","1010101010")
                        .setHeader("data", data)
                        .build())
                .expectStateChanged(1)
                .expectStates(L1ControllerState.HIT)
                .and()
                .build()
                .test();

        Mockito.verify(cpu, Mockito.times(1)).data(any());
        Mockito.verify(level2DataStore, Mockito.times(1)).isDataPresentInCache(any());
        Mockito.verify(level2DataStore, Mockito.never()).getBlockAtAddress(any(Address.class));
    }

    @Test
    public void testL2CPUReadScenario4() throws Exception {
        var data = new CacheBlock(1,2);

        when(level2DataStore.isDataPresentInCache(any(Address.class)))
                .thenReturn(DataResponseType.MISSD);

        StateMachineTestPlanBuilder.<L1ControllerState, L1InMessage>builder()
                .stateMachine(l2ControllerStateMachine)
                .step()
                .expectStates(L1ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(L1InMessage.CPUREAD)
                        .setHeader("source", "cpu")
                        .setHeader("address", "1010101010")
                        .setHeader("data", data)
                        .build())
                .expectStateChanged(3)
                .expectStates(L1ControllerState.RD2WAITD)
                .and()
                .step()
                // L2 Sends Data Back
                .sendEvent(MessageBuilder
                        .withPayload(L1InMessage.DATA)
                        .setHeader("source", "L2")
                        .setHeader("address", "1010101010")
                        .setHeader("data", data)
                        .build())
                .expectStateChanged(2)
                .expectStates(L1ControllerState.HIT)
                .and()
                .build()
                .test();

        Mockito.verify(cpu, atMostOnce()).data(any());
        Mockito.verify(level2DataStore, atMostOnce()).writeDataToCache(any(Address.class), any(CacheBlock.class));
// Add additional test once functionality has been established
//        Mockito.verify(l1DataRepository, Mockito.times(1)).victimize(any());
//        Mockito.verify(level2Controller, Mockito.times(1)).setData(any());
    }

    @Test
    public void testCpuWriteScenario1() throws Exception {
        when(level2DataStore.canWriteToCache(any(Address.class))).thenReturn(DataResponseType.HIT);
        Mockito.doNothing().when(level2DataStore).writeDataToCache(any(Address.class), any(CacheBlock.class));

        StateMachineTestPlanBuilder.<L1ControllerState, L1InMessage>builder()
                .stateMachine(l2ControllerStateMachine)
                .step()
                .expectStates(L1ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(L1InMessage.CPUWRITE)
                        .setHeader("source", "cpu")
                        .setHeader("address", "1010101010")
                        .setHeader("data", new byte[]{10, 20, 30, 40})
                        .build())
                .expectStateChanged(2)
                .expectStates(L1ControllerState.HIT)
                .and()
                .build()
                .test();

        Mockito.verify(level2DataStore, atMostOnce()).canWriteToCache(any(Address.class));
        Mockito.verify(level2DataStore, atMostOnce()).writeDataToCache(any(Address.class), any(CacheBlock.class));
    }

    @Test
    public void testCpuWriteScenario2() throws Exception {
        var data = new CacheBlock(1,2);
        when(level2DataStore.getBlockAtAddress(any(Address.class))).thenReturn(data);
        when(level2DataStore.canWriteToCache(any(Address.class)))
                .thenReturn(DataResponseType.MISSI)
                .thenReturn(DataResponseType.HIT);

        StateMachineTestPlanBuilder.<L1ControllerState, L1InMessage>builder()
                .stateMachine(l2ControllerStateMachine)
                .step()
                .expectStates(L1ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(L1InMessage.CPUWRITE)
                        .setHeader("source", "cpu")
                        .setHeader("address", "1010101010")
                        .setHeader("data", data)
                        .build())
                .expectStateChanged(3)
                .expectStates(L1ControllerState.WRWAITD)
                .and()
                .step()
                // L2 Sends Data Back
                .sendEvent(MessageBuilder
                        .withPayload(L1InMessage.DATA)
                        .setHeader("source", "L2")
                        .setHeader("address", "1010101010")
                        .setHeader("data", data)
                        .build())
                .expectStateChanged(2)
                .expectStates(L1ControllerState.HIT)
                .and()
                .build()
                .test();

        Mockito.verify(level2DataStore, Mockito.times(2)).canWriteToCache(any(Address.class));
        Mockito.verify(level2DataStore, Mockito.times(1)).writeDataToCache(any(Address.class), any(CacheBlock.class));
    }

    @Test
    public void testCpuWriteScenario3() throws Exception {
        var data = new CacheBlock(1,2);
        when(level2DataStore.getBlockAtAddress(any(Address.class))).thenReturn(data);
        when(level2DataStore.canWriteToCache(any(Address.class)))
                .thenReturn(DataResponseType.MISSC)
                .thenReturn(DataResponseType.HIT);

        StateMachineTestPlanBuilder.<L1ControllerState, L1InMessage>builder()
                .stateMachine(l2ControllerStateMachine)
                .step()
                .expectStates(L1ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(L1InMessage.CPUWRITE)
                        .setHeader("source", "cpu")
                        .setHeader("address", "1010101010")
                        .setHeader("data", data)
                        .build())
                .expectStateChanged(3)
                .expectStates(L1ControllerState.WRWAITD)
                .and()
                .step()
                // L2 Sends Data Back
                .sendEvent(MessageBuilder
                        .withPayload(L1InMessage.DATA)
                        .setHeader("source", "L2")
                        .setHeader("address", "101010101010")
                        .setHeader("data", data)
                        .build())
                .expectStateChanged(2)
                .expectStates(L1ControllerState.HIT)
                .and()
                .build()
                .test();

        Mockito.verify(level2DataStore, Mockito.times(2)).canWriteToCache(any(Address.class));
        Mockito.verify(level2DataStore, Mockito.times(1)).writeDataToCache(any(Address.class), any(CacheBlock.class));
    }

    @Test
    public void testCpuWriteScenario4() throws Exception {
        var data = new CacheBlock(1,2);

        when(level2DataStore.getBlockAtAddress(any(Address.class))).thenReturn(data);
        when(level2DataStore.canWriteToCache(any(Address.class)))
                .thenReturn(DataResponseType.MISSD)
                .thenReturn(DataResponseType.HIT);

        StateMachineTestPlanBuilder.<L1ControllerState, L1InMessage>builder()
                .stateMachine(l2ControllerStateMachine)
                .step()
                .expectStates(L1ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(L1InMessage.CPUWRITE)
                        .setHeader("source", "cpu")
                        .setHeader("address", "1010101010")
                        .setHeader("data", data)
                        .build())
                .expectStateChanged(3)
                .expectStates(L1ControllerState.WRWAIT2D)
                .and()
                .step()
                // L2 Sends Data Back
                .sendEvent(MessageBuilder
                        .withPayload(L1InMessage.DATA)
                        .setHeader("source", "L2")
                        .setHeader("address", "1010101010")
                        .setHeader("data", data)
                        .build())
                .expectStateChanged(3)
                .expectStates(L1ControllerState.HIT)
                .and()
                .build()
                .test();

        Mockito.verify(level2DataStore, Mockito.times( 2)).canWriteToCache(any(Address.class));
        Mockito.verify(level2DataStore, Mockito.times(1)).writeDataToCache(any(Address.class), any(CacheBlock.class));
    }
}

