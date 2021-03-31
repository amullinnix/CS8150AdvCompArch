package edu.uno.advcomparch.statemachine;

import edu.uno.advcomparch.controller.Address;
import edu.uno.advcomparch.controller.CacheBlock;
import edu.uno.advcomparch.controller.DataResponseType;
import edu.uno.advcomparch.cpu.CentralProcessingUnit;
import edu.uno.advcomparch.storage.Level1DataStore;
import edu.uno.advcomparch.storage.VictimCache;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;

@Configuration
@EnableStateMachine(name = "l1ControllerStateMachine")
@AllArgsConstructor
public class L1ControllerStateMachineConfiguration extends StateMachineConfigurerAdapter<ControllerState, ControllerMessage> {

    private static final int L1_TAG_SIZE = 6;
    private static final int L1_INDEX_SIZE = 6;
    private static final int L1_OFFSET_SIZE = 5;

    @Autowired
    StateMachineMessageBus messageBus;

    @Autowired
    CentralProcessingUnit<String> cpu;

    @Autowired
    Level1DataStore level1DataStore;

    @Autowired
    VictimCache l1VictimCache;

    @Override
    public void configure(StateMachineConfigurationConfigurer<ControllerState, ControllerMessage> config) throws Exception {
        config.withConfiguration()
                .autoStartup(false)
                .listener(l1listener());
    }

    @Bean
    public StateMachineListener<ControllerState, ControllerMessage> l1listener() {
        return new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<ControllerState, ControllerMessage> from, State<ControllerState, ControllerMessage> to) {
                if(from == null) {
                    System.out.println("L1 State change to " + to.getId());
                } else {
                    if(from.getId() != to.getId()) {
                        System.out.println("L1 State change from: " + from.getId() + " to " + to.getId());
                    }
                }
            }
        };
    }

    @Override
    public void configure(StateMachineStateConfigurer<ControllerState, ControllerMessage> states) throws Exception {
        states.withStates()
                .initial(ControllerState.HIT)
                .end(ControllerState.HIT)
                .state(ControllerState.HIT, processL1Message())
                .state(ControllerState.RDWAITD, L1CPURead())
                .state(ControllerState.RDL2WAITD, L2CCPURead())
                .state(ControllerState.WRWAITDX, L1Data())
                .state(ControllerState.WRALLOC, L1Data())
                .state(ControllerState.WRWAITD, L2CCPURead())
                .state(ControllerState.MISSC, L2CCPURead())
                .state(ControllerState.MISSD, L2CCPURead())
                .states(EnumSet.allOf(ControllerState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<ControllerState, ControllerMessage> transitions) throws Exception {
        transitions.withExternal()
                .source(ControllerState.HIT).event(ControllerMessage.CPUREAD)
                .target(ControllerState.RDWAITD)
                .and().withExternal()
                // Interesting, used this for polling
                .source(ControllerState.HIT).target(ControllerState.HIT)
                .and().withExternal()
                // Waiting on L2 Response
                .source(ControllerState.RDL2WAITD).target(ControllerState.RDL2WAITD)
                .and().withExternal()
                .source(ControllerState.RDWAITD).event(ControllerMessage.DATA)
                .target(ControllerState.HIT).action(CPUData())
                .and().withExternal()
                // Why is this not also a state, some form of miss if L1D is a separate component
                .source(ControllerState.RDWAITD).event(ControllerMessage.MISSI)
                .target(ControllerState.MISSI)
                .and().withExternal()
                .source(ControllerState.RDWAITD).event(ControllerMessage.MISSC)
                .target(ControllerState.MISSC)
                .and().withExternal()
                .source(ControllerState.RDWAITD).event(ControllerMessage.MISSD)
                .target(ControllerState.MISSD)
                .and().withExternal()
                // End of newly added configuration
                .source(ControllerState.MISSI).event(ControllerMessage.CPUREAD)
                .target(ControllerState.RDL2WAITD)
                .and().withExternal()
                .source(ControllerState.RDL2WAITD).target(ControllerState.HIT)
                .event(ControllerMessage.DATA).action(CPUData())
                .and().withExternal()
                .source(ControllerState.MISSC).event(ControllerMessage.CPUREAD)
                .target(ControllerState.RDL2WAITD)
                .and().withExternal()
                .source(ControllerState.MISSD).event(ControllerMessage.CPUREAD)
                .target(ControllerState.RD2WAITD)
                .and().withExternal()
                .source(ControllerState.RD2WAITD).event(ControllerMessage.DATA)
                .target(ControllerState.RD1WAITD) // no action
                .and().withExternal()
                .source(ControllerState.RD1WAITD).event(ControllerMessage.DATA)
                .target(ControllerState.HIT).action(L2CData()).action(CPUData())
                .and().withExternal()
                .source(ControllerState.HIT).event(ControllerMessage.CPUWRITE)
                .target(ControllerState.WRWAITDX)
                .and().withExternal()
                // Local States
                .source(ControllerState.WRWAITDX).event(ControllerMessage.DATA)
                .target(ControllerState.HIT)
                .and().withExternal()
                .source(ControllerState.WRWAITDX).event(ControllerMessage.MISSI)
                .target(ControllerState.MISSI)
                .and().withExternal()
                .source(ControllerState.WRWAITDX).event(ControllerMessage.MISSC)
                .target(ControllerState.MISSC)
                .and().withExternal()
                .source(ControllerState.WRWAITDX).event(ControllerMessage.MISSD)
                .target(ControllerState.MISSD)
                .and().withExternal()
                // End Local
                .source(ControllerState.MISSI).event(ControllerMessage.CPUWRITE)
                .target(ControllerState.WRWAITD)
                .and().withExternal()
                .source(ControllerState.WRWAITD).event(ControllerMessage.DATA)
                .target(ControllerState.WRALLOC)
                .and().withExternal()
                .source(ControllerState.WRALLOC).event(ControllerMessage.DATA)
                .target(ControllerState.HIT)
                .and().withExternal()
                .source(ControllerState.MISSC).event(ControllerMessage.CPUWRITE)
                .target(ControllerState.WRWAITD)//.action(L2CCPURead())
                .and().withExternal()
                .source(ControllerState.WRWAITD).event(ControllerMessage.DATA)
                .target(ControllerState.WRALLOC)
                .and().withExternal()
                .source(ControllerState.MISSD).event(ControllerMessage.CPUWRITE)
                .target(ControllerState.WRWAIT2D)//.action(L1Victimize()).action(L2CCPURead())
                .and().withExternal()
                .source(ControllerState.WRWAIT2D).event(ControllerMessage.DATA)
                .target(ControllerState.WRWAIT1D)
                .and().withExternal()
                .source(ControllerState.WRWAIT1D).event(ControllerMessage.DATA)
                .target(ControllerState.WRALLOC).action(L2CData());
    }

    @Bean
    public Action<ControllerState, ControllerMessage> L1CPURead() {
        return ctx -> {
            // If queue is non empty, on state transition perform one action.
            var message = ctx.getMessage();
            var address = message.getHeaders().get("address", String.class);
            var bytes = message.getHeaders().get("bytes", Integer.class);

            System.out.println("CPU to L1C: CPURead(" + address + ")");

            var partitionedAddress = new Address(address);
            partitionedAddress.componentize(L1_TAG_SIZE, L1_INDEX_SIZE, L1_OFFSET_SIZE);

            var canRead = level1DataStore.isDataPresentInCache(partitionedAddress);
            var data = level1DataStore.getDataAtAddress(partitionedAddress, bytes);

            // If we get nothing back send miss.
            if (canRead == DataResponseType.HIT) {

                var responseMessage = MessageBuilder
                        .withPayload(ControllerMessage.DATA)
                        .setHeader("source", "L1Data")
                        .setHeader("address", partitionedAddress.getAddress())
                        .setHeader("data", data)
                        .build();

                // Send successful message back to the controller
                ctx.getStateMachine().sendEvent(responseMessage);

            } else {

                // Check to see if we can pull from Victim Cache before forwarding
                var victimCacheBlock = l1VictimCache.getData(partitionedAddress);

                if (victimCacheBlock != null) {
                    var victimCacheMessage = MessageBuilder
                            .withPayload(ControllerMessage.DATA)
                            .setHeader("source", "L1Data")
                            .setHeader("address", victimCacheBlock.getAddress())
                            .setHeader("data", victimCacheBlock.getBlock())
                            .build();

                    // Send successful message back to the controller
                    ctx.getStateMachine().sendEvent(victimCacheMessage);

                    // Fail out of further error behavior;
                    return;
                }

                // Else we have a MISSI or MISSC
                // Send a miss to transition to miss state
                var missMessage = MessageBuilder
                        .withPayload(ControllerMessage.fromControllerState(canRead))
                        .setHeader("source", "L1Data")
                        .build();

                ctx.getStateMachine().sendEvent(missMessage);

                // Then send a read back
                var cpuReadMessage = MessageBuilder
                        .withPayload(ControllerMessage.CPUREAD)
                        .setHeader("source", "L1Data")
                        .setHeader("address", partitionedAddress.getAddress())
                        .setHeader("bytes", bytes)
                        .build();

                ctx.getStateMachine().sendEvent(cpuReadMessage);

                if (canRead == DataResponseType.MISSD) {

                    System.out.println("L1C to L1D: Victimize(" + address + ")");

                    var victimizeResponseMessage = MessageBuilder
                            .withPayload(ControllerMessage.DATA)
                            .setHeader("source", "L1Data")
                            .setHeader("address", address)
                            .setHeader("data", data)
                            .build();

                    ctx.getStateMachine().sendEvent(victimizeResponseMessage);
                }
            }
        };
    }

    @Bean
    public Action<ControllerState, ControllerMessage> L1Data() {
        return ctx -> {
            var message = ctx.getMessage();
            var address = message.getHeaders().get("address", String.class);
            var messageData = message.getHeaders().get("data");

            var data = Optional.of(messageData)
                    .filter(CacheBlock.class::isInstance)
                    .map(CacheBlock.class::cast)
                    .map(CacheBlock::getBlock)
                    .orElse((byte[]) messageData);

            var l1Address = new Address(address);
            l1Address.componentize(L1_TAG_SIZE, L1_INDEX_SIZE, L1_OFFSET_SIZE);

            var canWrite = level1DataStore.canWriteToCache(l1Address);

            if (canWrite == DataResponseType.HIT) {
                System.out.println("L1C to L1D: Write(" + Arrays.toString(data) + ")");

                var state = ctx.getStateMachine().getState().getId();
                if (ControllerState.READ_STATES.contains(state)) {
                    level1DataStore.writeDataToCacheTriggeredByRead(l1Address, data);
                } else if (ControllerState.WRITE_STATES.contains(state)) {
                    level1DataStore.writeDataToCache(l1Address, data);
                } else {
                    throw new UnsupportedOperationException("L1D Write Operation received from unrecognized state." + state.toString());
                }

                var responseMessage = MessageBuilder
                        .withPayload(ControllerMessage.DATA)
                        .setHeader("source", "L1Data")
                        .setHeader("address", l1Address.getAddress())
                        .setHeader("data", data)
                        .build();

                // Send successful message back to the controller
                ctx.getStateMachine().sendEvent(responseMessage);
            } else {
                var missMessage = MessageBuilder
                        .withPayload(ControllerMessage.fromControllerState(canWrite))
                        .setHeader("source", "L1Data")
                        .build();

                ctx.getStateMachine().sendEvent(missMessage);

                var cpuWriteMessage = MessageBuilder
                        .withPayload(ControllerMessage.CPUWRITE)
                        .setHeader("source", "L1Data")
                        .setHeader("address", l1Address.getAddress())
                        .setHeader("data", data)
                        .build();

                ctx.getStateMachine().sendEvent(cpuWriteMessage);

                if (canWrite == DataResponseType.MISSD) {

                    System.out.println("Victimize L1C to L1D (" + address + ")");

                    var victimBlock = l1VictimCache.getData(l1Address);

                    // Send victim data Request Back to L1D with Victimized Data to be replicated to L2C
                    var victimizeResponseMessage = MessageBuilder
                            .withPayload(ControllerMessage.DATA)
                            .setHeader("source", "L1Data")
                            .setHeader("address", l1Address)
                            .setHeader("data", victimBlock)
                            .build();

                    ctx.getStateMachine().sendEvent(victimizeResponseMessage);
                }
            }
        };
    }

    @Bean
    public Action<ControllerState, ControllerMessage> L2CCPURead() {
        return ctx -> {

            var waitingOnL2 = ctx.getExtendedState().get("waitingOnL2", Boolean.class);

            // If we haven't transitioned
            if (waitingOnL2 != Boolean.TRUE) {

                var message = ctx.getMessage();
                var address = message.getHeaders().get("address", String.class);
                var bytes = message.getHeaders().get("bytes", Integer.class);

                var partitionedAddress = new Address(address);
                partitionedAddress.componentize(L1_TAG_SIZE, L1_INDEX_SIZE, L1_OFFSET_SIZE);

                System.out.println("L1C to L2C: CpuRead(" + address + ")");

                // Place on the L2 Message Queue
                messageBus.enqueueL2Message(message);

                ctx.getExtendedState().getVariables().put("waitingOnL2", Boolean.TRUE);
            } else {
                // Poll for L2 Response
                var l2Message = messageBus.getL1MessageQueue().poll();

                if (l2Message != null) {
                    System.out.println("Message Received on L1 from L2 Side, sending event: " + l2Message.getPayload());
                    ctx.getStateMachine().sendEvent(l2Message);
                    ctx.getExtendedState().getVariables().put("waitingOnL2", Boolean.FALSE);
                }
            }


        };
    }

    @Bean
    public Action<ControllerState, ControllerMessage> L2CData() {
        return ctx -> {
            var message = ctx.getMessage();
            var data = (byte[]) message.getHeaders().get("data");
            System.out.println("L1C to L2C: Data(" + Arrays.toString(data) + ")");

            messageBus.enqueueL2Message(message);
        };
    }

    @Bean
    public Action<ControllerState, ControllerMessage> CPUData() {
        return ctx -> {
            var data = ctx.getMessage().getHeaders().get("data", CacheBlock.class);
            System.out.println("L1C to CPU: Data(" + Arrays.toString(data.getBlock()) + ")");

            // report back to the cpu
            cpu.data(data.getBlock());
        };
    }

    @Bean
    public Action<ControllerState, ControllerMessage> processL1Message() {
        return ctx -> {
//            System.out.println("Attempting to poll L1 queue");
            var stateMachine = ctx.getStateMachine();
            var currentState = stateMachine.getState().getId();

            if (currentState == ControllerState.HIT) {
                // If we have a message, start processing
                var cpuMessage = messageBus.getCPUMessageQueue().poll();

                if (cpuMessage != null) {
                    System.out.println("Message Received on L1 from CPU Side, sending event: " + cpuMessage.getPayload());
                    stateMachine.sendEvent(cpuMessage);
                } else if (messageBus.getL1MessageQueue().isEmpty()) {
                    // If we've completed our L1 Instruction queue stop l1.
//                    ctx.getStateMachine().stop();
                }
            } else {
                var l2Message = messageBus.getL1MessageQueue().poll();

                if (l2Message != null) {
                    System.out.println("Message Received on L1 from L2 Side, sending event: " + l2Message.getPayload());
                    stateMachine.sendEvent(l2Message);
                }
            }
        };
    }
}