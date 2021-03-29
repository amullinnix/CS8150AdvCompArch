package edu.uno.advcomparch.state_machine;

import edu.uno.advcomparch.AbstractCompArchTest;
import edu.uno.advcomparch.config.L1StateMachineTestConfiguration;
import edu.uno.advcomparch.controller.Address;
import edu.uno.advcomparch.controller.CacheBlock;
import edu.uno.advcomparch.controller.DataResponseType;
import edu.uno.advcomparch.cpu.DefaultCPU;
import edu.uno.advcomparch.statemachine.ControllerMessage;
import edu.uno.advcomparch.statemachine.ControllerState;
import edu.uno.advcomparch.statemachine.StateMachineMessageBus;
import edu.uno.advcomparch.storage.Level1DataStore;
import edu.uno.advcomparch.storage.VictimCache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import org.springframework.test.context.ContextConfiguration;

import java.util.LinkedList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = L1StateMachineTestConfiguration.class)
public class L1StateMachineTests extends AbstractCompArchTest {

    @Autowired
    Level1DataStore level1DataStore;

    @Autowired
    DefaultCPU cpu;

    @Autowired
    VictimCache victimCache;

    @Autowired
    StateMachineMessageBus stateMachineMessageBus;

    @Autowired
    private StateMachine<ControllerState, ControllerMessage> l1ControllerStateMachine;

    @BeforeEach
    public void beforeEach() {
        var data = new byte[]{10, 20, 30, 40};
        when(level1DataStore.getDataAtAddress(any(Address.class), anyInt()))
                .thenReturn(data);
        when(level1DataStore.canWriteToCache(any(Address.class)))
                .thenReturn(DataResponseType.HIT);
        when(level1DataStore.isDataPresentInCache(any(Address.class)))
                .thenReturn(DataResponseType.HIT);

        when(victimCache.getData(any(Address.class))).thenReturn(null);
        when(stateMachineMessageBus.getL1MessageQueue()).thenReturn(new LinkedList<>());

        l1ControllerStateMachine.start();
    }

    @AfterEach
    public void afterEach() {
        l1ControllerStateMachine.stop();
        reset(cpu, level1DataStore, stateMachineMessageBus, victimCache);
    }

    @Test
    public void testStartEvent() {
        assertThat(l1ControllerStateMachine.getState().getId()).isEqualTo(ControllerState.HIT);
    }

    @Test
    public void testL1HitCPUReadScenario1() throws Exception {
        StateMachineTestPlanBuilder.<ControllerState, ControllerMessage>builder()
                .stateMachine(l1ControllerStateMachine)
                .step()
                .expectStates(ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(ControllerMessage.CPUREAD)
                        .setHeader("source", "cpu")
                        .setHeader("address", "10101010101010101")
                        .setHeader("bytes", 4)
                        .build())
                .expectStateChanged(2)
                .expectStates(ControllerState.HIT)
                .and()
                .build()
                .test();

        verify(cpu, atMostOnce()).data(any());
        verify(level1DataStore, atMostOnce()).isDataPresentInCache(any());
        verify(level1DataStore, atMostOnce()).getDataAtAddress(any(Address.class), anyInt());
    }

    @Test
    public void testL1MissCCpuReadScenario2() throws Exception {
        when(level1DataStore.isDataPresentInCache(any(Address.class)))
                .thenReturn(DataResponseType.MISSC);

        StateMachineTestPlanBuilder.<ControllerState, ControllerMessage>builder()
                .stateMachine(l1ControllerStateMachine)
                .step()
                .expectStates(ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(ControllerMessage.CPUREAD)
                        .setHeader("source", "cpu")
                        .setHeader("address", "10101010101010101")
                        .setHeader("bytes", 4)
                        .build())
                .expectStateChanged(3)
                .expectStates(ControllerState.RDL2WAITD)
                .and()
                .build()
                .test();

        // If we've missed then enqueue
        verify(stateMachineMessageBus, atMostOnce()).enqueueL2Message(any());
    }

    @Test
    public void testL1MissCCpuReadScenario2WithVictimCache() throws Exception {
        when(level1DataStore.isDataPresentInCache(any(Address.class)))
                .thenReturn(DataResponseType.MISSC);

        var data = new byte[]{10, 20, 30, 40};
        var victimCacheBlock = new CacheBlock(4,4);
        victimCacheBlock.setBlock(data);
        victimCacheBlock.setAddress(new Address("101"));

        when(victimCache.getData(any(Address.class))).thenReturn(victimCacheBlock);

        StateMachineTestPlanBuilder.<ControllerState, ControllerMessage>builder()
                .stateMachine(l1ControllerStateMachine)
                .step()
                .expectStates(ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(ControllerMessage.CPUREAD)
                        .setHeader("source", "cpu")
                        .setHeader("address", "10101010101010101")
                        .setHeader("bytes", 4)
                        .build())
                .expectStateChanged(2)
                .expectStates(ControllerState.HIT)
                .and()
                .build()
                .test();

        // Make sure we haven't queued the next instruction.
        verify(stateMachineMessageBus, never()).enqueueL2Message(any());
        verify(victimCache, atMostOnce()).getData(any(Address.class));
    }

    @Test
    public void testL1MissICpuReadScenario3() throws Exception {
        when(level1DataStore.isDataPresentInCache(any(Address.class)))
                .thenReturn(DataResponseType.MISSI);

        StateMachineTestPlanBuilder.<ControllerState, ControllerMessage>builder()
                .stateMachine(l1ControllerStateMachine)
                .step()
                .expectStates(ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(ControllerMessage.CPUREAD)
                        .setHeader("source", "cpu")
                        .setHeader("address", "10101010101010101")
                        .setHeader("bytes", 4)
                        .build())
                .expectStateChanged(3)
                .expectStates(ControllerState.RDL2WAITD)
                .and()
                .build()
                .test();

        // If we've missed then enqueue
        verify(stateMachineMessageBus, atMostOnce()).enqueueL2Message(any());
    }

    @Test
    public void testL1MissD() throws Exception {
        when(level1DataStore.isDataPresentInCache(any(Address.class)))
                .thenReturn(DataResponseType.MISSD);

        StateMachineTestPlanBuilder.<ControllerState, ControllerMessage>builder()
                .stateMachine(l1ControllerStateMachine)
                .step()
                .expectStates(ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(ControllerMessage.CPUREAD)
                        .setHeader("source", "cpu")
                        .setHeader("address", "10101010101010101")
                        .setHeader("bytes", 4)
                        .build())
                .expectStateChanged(3)
                .expectStates(ControllerState.RD2WAITD)
                .and()
                .build()
                .test();
    }

    @Test
    public void testL2CPUReadScenario2() throws Exception {
        var data = new byte[]{10, 20, 30, 40};
        when(level1DataStore.isDataPresentInCache(any(Address.class)))
                .thenReturn(DataResponseType.MISSI);

        StateMachineTestPlanBuilder.<ControllerState, ControllerMessage>builder()
                .stateMachine(l1ControllerStateMachine)
                .step()
                .expectStates(ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(ControllerMessage.CPUREAD)
                        .setHeader("source", "cpu")
                        .setHeader("address", "10101010101010101")
                        .setHeader("bytes", 4)
                        .build())
                .expectStateChanged(3)
                .expectStates(ControllerState.RDL2WAITD)
                .and()
                .step()
                // L2 Sends Data Back
                .sendEvent(MessageBuilder
                        .withPayload(ControllerMessage.DATA)
                        .setHeader("source", "L2")
                        .setHeader("address","10101010101010101")
                        .setHeader("data", data)
                        .build())
                .expectStateChanged(1)
                .expectStates(ControllerState.HIT)
                .and()
                .build()
                .test();

        verify(stateMachineMessageBus, atMostOnce()).enqueueL2Message(any());
        verify(cpu, atMostOnce()).data(any());
        verify(level1DataStore, atMostOnce()).isDataPresentInCache(any());
        verify(level1DataStore, atMostOnce()).getDataAtAddress(any(Address.class), anyInt());
        verify(level1DataStore, atMostOnce()).writeDataToCacheTriggeredByRead(any(Address.class), any());
    }

    @Test
    public void testL2CPUReadScenario4() throws Exception {
        var data = new byte[]{10, 20, 30, 40};

        when(level1DataStore.isDataPresentInCache(any(Address.class)))
                .thenReturn(DataResponseType.MISSD);

        StateMachineTestPlanBuilder.<ControllerState, ControllerMessage>builder()
                .stateMachine(l1ControllerStateMachine)
                .step()
                .expectStates(ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(ControllerMessage.CPUREAD)
                        .setHeader("source", "cpu")
                        .setHeader("address", "10101010101010101")
                        .setHeader("bytes", 4)
                        .build())
                .expectStateChanged(4)
                .expectStates(ControllerState.RD1WAITD)
                .and()
                .step()
                // L2 Sends Data Back
                .sendEvent(MessageBuilder
                        .withPayload(ControllerMessage.DATA)
                        .setHeader("source", "L2")
                        .setHeader("address", "10101010101010101")
                        .setHeader("data", data)
                        .build())
                .expectStateChanged(1)
                .expectStates(ControllerState.HIT)
                .and()
                .build()
                .test();

        verify(stateMachineMessageBus, atMostOnce()).enqueueL2Message(any());
        verify(cpu, atMostOnce()).data(any());
        verify(level1DataStore, atMostOnce()).writeDataToCache(any(Address.class), any());
        verify(level1DataStore, atMostOnce()).writeDataToCacheTriggeredByRead(any(Address.class), any());
    }

    @Test
    public void testCpuWriteScenario1() throws Exception {
        when(level1DataStore.canWriteToCache(any(Address.class))).thenReturn(DataResponseType.HIT);
        doNothing().when(level1DataStore).writeDataToCache(any(Address.class), any());

        StateMachineTestPlanBuilder.<ControllerState, ControllerMessage>builder()
                .stateMachine(l1ControllerStateMachine)
                .step()
                .expectStates(ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(ControllerMessage.CPUWRITE)
                        .setHeader("source", "cpu")
                        .setHeader("address", "10101010101010101")
                        .setHeader("data", new byte[]{10, 20, 30, 40})
                        .build())
                .expectStateChanged(2)
                .expectStates(ControllerState.HIT)
                .and()
                .build()
                .test();

        verify(level1DataStore, atMostOnce()).canWriteToCache(any(Address.class));
        verify(level1DataStore, atMostOnce()).writeDataToCache(any(Address.class), any());
    }

    @Test
    public void testCpuWriteScenario2() throws Exception {
        var data = new byte[]{10, 20, 30, 40};
        when(level1DataStore.getDataAtAddress(any(Address.class), anyInt())).thenReturn(data);
        when(level1DataStore.canWriteToCache(any(Address.class)))
                .thenReturn(DataResponseType.MISSI)
                .thenReturn(DataResponseType.HIT);

        StateMachineTestPlanBuilder.<ControllerState, ControllerMessage>builder()
                .stateMachine(l1ControllerStateMachine)
                .step()
                .expectStates(ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(ControllerMessage.CPUWRITE)
                        .setHeader("source", "cpu")
                        .setHeader("address", "10101010101010101")
                        .setHeader("data", data)
                        .build())
                .expectStateChanged(3)
                .expectStates(ControllerState.WRWAITD)
                .and()
                .step()
                // L2 Sends Data Back
                .sendEvent(MessageBuilder
                        .withPayload(ControllerMessage.DATA)
                        .setHeader("source", "L2")
                        .setHeader("address", "10101010101010101")
                        .setHeader("data", data)
                        .build())
                .expectStateChanged(2)
                .expectStates(ControllerState.HIT)
                .and()
                .build()
                .test();

        verify(level1DataStore, times(2)).canWriteToCache(any(Address.class));
        verify(level1DataStore, atMostOnce()).writeDataToCache(any(), any());
    }

    @Test
    public void testCpuWriteScenario3() throws Exception {
        var data = new byte[]{10, 20, 30, 40};
        when(level1DataStore.getDataAtAddress(any(Address.class), anyInt())).thenReturn(data);
        when(level1DataStore.canWriteToCache(any(Address.class)))
                .thenReturn(DataResponseType.MISSC)
                .thenReturn(DataResponseType.HIT);

        StateMachineTestPlanBuilder.<ControllerState, ControllerMessage>builder()
                .stateMachine(l1ControllerStateMachine)
                .step()
                .expectStates(ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(ControllerMessage.CPUWRITE)
                        .setHeader("source", "cpu")
                        .setHeader("address", "10101010101010101")
                        .setHeader("data", data)
                        .build())
                .expectStateChanged(3)
                .expectStates(ControllerState.WRWAITD)
                .and()
                .step()
                // L2 Sends Data Back
                .sendEvent(MessageBuilder
                        .withPayload(ControllerMessage.DATA)
                        .setHeader("source", "L2")
                        .setHeader("address", "10101010101010101")
                        .setHeader("data", data)
                        .build())
                .expectStateChanged(2)
                .expectStates(ControllerState.HIT)
                .and()
                .build()
                .test();

        verify(level1DataStore, times(2)).canWriteToCache(any(Address.class));
        verify(level1DataStore, atMostOnce()).writeDataToCache(any(), any());
    }

    @Test
    public void testCpuWriteScenario4() throws Exception {
        var data = new byte[]{10, 20, 30, 40};

        when(level1DataStore.getDataAtAddress(any(Address.class), anyInt())).thenReturn(data);
        when(level1DataStore.canWriteToCache(any(Address.class)))
                .thenReturn(DataResponseType.MISSD)
                .thenReturn(DataResponseType.HIT);

        StateMachineTestPlanBuilder.<ControllerState, ControllerMessage>builder()
                .stateMachine(l1ControllerStateMachine)
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(ControllerMessage.CPUWRITE)
                        .setHeader("source", "cpu")
                        .setHeader("address", "10101010101010101")
                        .setHeader("data", data)
                        .build())
                .expectStateChanged(4)
                .expectStates(ControllerState.WRWAIT1D)
                .and()
                .step()
                // L2 Sends Data Back
                .sendEvent(MessageBuilder
                        .withPayload(ControllerMessage.DATA)
                        .setHeader("source", "L2")
                        .setHeader("address", "10101010101010101")
                        .setHeader("data", data)
                        .build())
                .expectStateChanged(2)
                .expectStates(ControllerState.HIT)
                .and()
                .build()
                .test();

        verify(level1DataStore, times( 2)).canWriteToCache(any(Address.class));
        verify(level1DataStore, atMostOnce()).writeDataToCache(any(), any());
        verify(victimCache, atMostOnce()).getData(any(Address.class));
    }
}
