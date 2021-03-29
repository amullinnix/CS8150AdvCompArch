package edu.uno.advcomparch.state_machine;

import edu.uno.advcomparch.AbstractCompArchTest;
import edu.uno.advcomparch.config.L2StateMachineTestConfiguration;
import edu.uno.advcomparch.controller.Address;
import edu.uno.advcomparch.controller.CacheBlock;
import edu.uno.advcomparch.controller.DataResponseType;
import edu.uno.advcomparch.statemachine.ControllerMessage;
import edu.uno.advcomparch.statemachine.ControllerState;
import edu.uno.advcomparch.statemachine.StateMachineMessageBus;
import edu.uno.advcomparch.storage.DynamicRandomAccessMemory;
import edu.uno.advcomparch.storage.Level1WriteBuffer;
import edu.uno.advcomparch.storage.Level2DataStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import org.springframework.test.context.ContextConfiguration;

import java.util.LinkedList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = L2StateMachineTestConfiguration.class)
public class L2StateMachineTests extends AbstractCompArchTest {

    @Autowired
    Level2DataStore level2DataStore;

    @Autowired
    StateMachineMessageBus stateMachineMessageBus;

    @Autowired
    Level1WriteBuffer level1WriteBuffer;

    @Autowired
    DynamicRandomAccessMemory memory;

    @Autowired
    private StateMachine<ControllerState, ControllerMessage> l2ControllerStateMachine;

    @BeforeEach
    public void beforeEach() {
        when(level2DataStore.canWriteToCache(any(Address.class)))
                .thenReturn(DataResponseType.HIT);
        when(level2DataStore.isDataPresentInCache(any(Address.class)))
                .thenReturn(DataResponseType.HIT);
        var cacheBlock = new CacheBlock(1,2);
        cacheBlock.setBlock(new byte[]{10, 20, 30, 40});
        when(level2DataStore.getBlockAtAddress(any(Address.class)))
                .thenReturn(cacheBlock);

        when(level1WriteBuffer.getData(any(Address.class))).thenReturn(cacheBlock);
        when(memory.getMemoryAtAddress(any(Address.class))).thenReturn(cacheBlock);

        when(stateMachineMessageBus.getL2MessageQueue()).thenReturn(new LinkedList<>());

        l2ControllerStateMachine.start();
    }

    @AfterEach
    public void afterEach() {
        l2ControllerStateMachine.stop();
        reset(level1WriteBuffer, level2DataStore, stateMachineMessageBus);
    }

    @Test
    public void testStartEvent() {
        assertThat(l2ControllerStateMachine.getState().getId()).isEqualTo(ControllerState.HIT);
    }

    @Test
    public void testL2HitCPUReadScenario1() throws Exception {
        StateMachineTestPlanBuilder.<ControllerState, ControllerMessage>builder()
                .stateMachine(l2ControllerStateMachine)
                .step()
                .expectStates(ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(ControllerMessage.CPUREAD)
                        .setHeader("source", "L1")
                        .setHeader("address", "10101010101010101")
                        .build())
                .expectStateChanged(2)
                .expectStates(ControllerState.HIT)
                .and()
                .build()
                .test();

        Mockito.verify(stateMachineMessageBus, Mockito.atMostOnce()).enqueueL1Message(any());
        Mockito.verify(level2DataStore, Mockito.atMostOnce()).isDataPresentInCache(any());
        Mockito.verify(level2DataStore, Mockito.atMostOnce()).getBlockAtAddress(any(Address.class));
    }

    @Test
    public void testL1MissCCpuReadScenario2() throws Exception {
        when(level2DataStore.isDataPresentInCache(any(Address.class)))
                .thenReturn(DataResponseType.MISSC);

        StateMachineTestPlanBuilder.<ControllerState, ControllerMessage>builder()
                .stateMachine(l2ControllerStateMachine)
                .step()
                .expectStates(ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(ControllerMessage.CPUREAD)
                        .setHeader("source", "L1C")
                        .setHeader("address", "10101010101010101")
                        .build())
                .expectStateChanged(4)
                .expectStates(ControllerState.HIT)
                .and()
                .build()
                .test();
    }

    @Test
    public void testL1MissICpuReadScenario3() throws Exception {
        when(level2DataStore.isDataPresentInCache(any(Address.class)))
                .thenReturn(DataResponseType.MISSI);

        StateMachineTestPlanBuilder.<ControllerState, ControllerMessage>builder()
                .stateMachine(l2ControllerStateMachine)
                .step()
                .expectStates(ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(ControllerMessage.CPUREAD)
                        .setHeader("source", "L1C")
                        .setHeader("address", "10101010101010101")
                        .build())
                .expectStateChanged(4)
                .expectStates(ControllerState.HIT)
                .and()
                .build()
                .test();
    }

    @Test
    public void testL1MissDScenario4() throws Exception {
        when(level2DataStore.isDataPresentInCache(any(Address.class)))
                .thenReturn(DataResponseType.MISSD);

        StateMachineTestPlanBuilder.<ControllerState, ControllerMessage>builder()
                .stateMachine(l2ControllerStateMachine)
                .step()
                .expectStates(ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(ControllerMessage.CPUREAD)
                        .setHeader("source", "L1C")
                        .setHeader("address", "10101010101010101")
                        .build())
                .expectStateChanged(5)
                .expectStates(ControllerState.HIT)
                .and()
                .build()
                .test();

        Mockito.verify(level2DataStore, atMostOnce()).writeDataToCache(any(Address.class), any(CacheBlock.class));
    }

    @Test
    public void testCpuWriteScenario1() throws Exception {
        var cacheBlock = new CacheBlock(1,2);
        cacheBlock.setBlock(new byte[]{10, 20, 30, 40});

        when(level2DataStore.canWriteToCache(any(Address.class))).thenReturn(DataResponseType.HIT);
        Mockito.doNothing().when(level2DataStore).writeDataToCache(any(Address.class), any(CacheBlock.class));

        StateMachineTestPlanBuilder.<ControllerState, ControllerMessage>builder()
                .stateMachine(l2ControllerStateMachine)
                .step()
                .expectStates(ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(ControllerMessage.CPUWRITE)
                        .setHeader("source", "L1C")
                        .setHeader("address", "10101010101010101")
                        .setHeader("data", cacheBlock)
                        .build())
                .expectStateChanged(2)
                .expectStates(ControllerState.HIT)
                .and()
                .build()
                .test();

        Mockito.verify(level2DataStore, atMostOnce()).canWriteToCache(any(Address.class));
        Mockito.verify(level2DataStore, atMostOnce()).writeDataToCache(any(Address.class), any(CacheBlock.class));
    }

    @Test
    public void testCpuWriteScenario2() throws Exception {
        var cacheBlock = new CacheBlock(1,2);
        cacheBlock.setBlock(new byte[]{10, 20, 30, 40});

        when(level2DataStore.getBlockAtAddress(any(Address.class))).thenReturn(cacheBlock);
        when(level2DataStore.canWriteToCache(any(Address.class)))
                .thenReturn(DataResponseType.MISSI)
                .thenReturn(DataResponseType.HIT);

        StateMachineTestPlanBuilder.<ControllerState, ControllerMessage>builder()
                .stateMachine(l2ControllerStateMachine)
                .step()
                .expectStates(ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(ControllerMessage.CPUWRITE)
                        .setHeader("source", "L1C")
                        .setHeader("address", "10101010101010101")
                        .setHeader("data", cacheBlock)
                        .build())
                .expectStateChanged(5)
                .expectStates(ControllerState.HIT)
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

        StateMachineTestPlanBuilder.<ControllerState, ControllerMessage>builder()
                .stateMachine(l2ControllerStateMachine)
                .step()
                .expectStates(ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(ControllerMessage.CPUWRITE)
                        .setHeader("source", "L1C")
                        .setHeader("address", "10101010101010101")
                        .setHeader("data", data)
                        .build())
                .expectStateChanged(5)
                .expectStates(ControllerState.HIT)
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

        StateMachineTestPlanBuilder.<ControllerState, ControllerMessage>builder()
                .stateMachine(l2ControllerStateMachine)
                .step()
                .expectStates(ControllerState.HIT)
                .and()
                .step()
                .sendEvent(MessageBuilder
                        .withPayload(ControllerMessage.CPUWRITE)
                        .setHeader("source", "L1C")
                        .setHeader("address", "10101010101010101")
                        .setHeader("data", data)
                        .build())
                .expectStateChanged(6)
                .expectStates(ControllerState.HIT)
                .and()
                .build()
                .test();

        Mockito.verify(level2DataStore, Mockito.times( 2)).canWriteToCache(any(Address.class));
        Mockito.verify(level2DataStore, Mockito.times(1)).writeDataToCache(any(Address.class), any(CacheBlock.class));
    }
}

