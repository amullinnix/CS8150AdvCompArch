package edu.uno.advcomparch.statemachine;

import edu.uno.advcomparch.controller.Address;
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

@Configuration
@EnableStateMachine(name = "l1ControllerStateMachine")
@AllArgsConstructor
public class L1ControllerStateMachineConfiguration extends StateMachineConfigurerAdapter<L1ControllerState, L1InMessage> {

    private static final int L1_TAG_SIZE = 1;
    private static final int L1_INDEX_SIZE = 1;
    private static final int L1_OFFSET_SIZE = 1;

    @Autowired
    StateMachineMessageBus messageBus;

    @Autowired
    CentralProcessingUnit<String> cpu;

    @Autowired
    Level1DataStore level1DataStore;

    @Autowired
    VictimCache l1VictimCache;

    @Override
    public void configure(StateMachineConfigurationConfigurer<L1ControllerState, L1InMessage> config) throws Exception {
        config.withConfiguration()
                .autoStartup(false)
                .listener(l1listener());
    }

    @Bean
    public StateMachineListener<L1ControllerState, L1InMessage> l1listener() {
        return new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<L1ControllerState, L1InMessage> from, State<L1ControllerState, L1InMessage> to) {
                System.out.println("State change to " + to.getId());
            }
        };
    }

    @Override
    public void configure(StateMachineStateConfigurer<L1ControllerState, L1InMessage> states) throws Exception {
        states.withStates()
                .initial(L1ControllerState.HIT)
                .end(L1ControllerState.HIT)
                .state(L1ControllerState.HIT, processL1Message())
                .state(L1ControllerState.RDWAITD, L1CPURead())
                .state(L1ControllerState.RDL2WAITD, L2CCPURead())
                .state(L1ControllerState.WRWAITDX, L1Data())
                .state(L1ControllerState.WRALLOC, L1Data())
                .state(L1ControllerState.WRWAITD, L2CCPURead())
                .state(L1ControllerState.MISSC, L2CCPURead())
                .state(L1ControllerState.MISSD, L2CCPURead())
                .states(EnumSet.allOf(L1ControllerState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<L1ControllerState, L1InMessage> transitions) throws Exception {
        transitions.withExternal()
                .source(L1ControllerState.HIT).event(L1InMessage.CPUREAD)
                .target(L1ControllerState.RDWAITD)
                .and().withExternal()
                .source(L1ControllerState.RDWAITD).event(L1InMessage.DATA)
                .target(L1ControllerState.HIT).action(CPUData())
                .and().withExternal()
                // Why is this not also a state, some form of miss if L1D is a separate component
                .source(L1ControllerState.RDWAITD).event(L1InMessage.MISSI)
                .target(L1ControllerState.MISSI)
                .and().withExternal()
                .source(L1ControllerState.RDWAITD).event(L1InMessage.MISSC)
                .target(L1ControllerState.MISSC)
                .and().withExternal()
                .source(L1ControllerState.RDWAITD).event(L1InMessage.MISSD)
                .target(L1ControllerState.MISSD)
                .and().withExternal()
                // End of newly added configuration
                .source(L1ControllerState.MISSI).event(L1InMessage.CPUREAD)
                .target(L1ControllerState.RDL2WAITD)
                .and().withExternal()
                .source(L1ControllerState.RDL2WAITD).target(L1ControllerState.HIT)
                .event(L1InMessage.DATA).action(L1Data()).action(CPUData())
                .and().withExternal()
                .source(L1ControllerState.MISSC).event(L1InMessage.CPUREAD)
                .target(L1ControllerState.RDL2WAITD)
                .and().withExternal()
                .source(L1ControllerState.MISSD).event(L1InMessage.CPUREAD)
                .target(L1ControllerState.RD2WAITD)
                .and().withExternal()
                .source(L1ControllerState.RD2WAITD).event(L1InMessage.DATA)
                .target(L1ControllerState.RD1WAITD) // no action
                .and().withExternal()
                .source(L1ControllerState.RD1WAITD).event(L1InMessage.DATA)
                .target(L1ControllerState.HIT).action(L1Data()).action(L2CData()).action(CPUData())
                .and().withExternal()
                .source(L1ControllerState.HIT).event(L1InMessage.CPUWRITE)
                .target(L1ControllerState.WRWAITDX)
                .and().withExternal()
                // Local States
                .source(L1ControllerState.WRWAITDX).event(L1InMessage.DATA)
                .target(L1ControllerState.HIT)
                .and().withExternal()
                .source(L1ControllerState.WRWAITDX).event(L1InMessage.MISSI)
                .target(L1ControllerState.MISSI)
                .and().withExternal()
                .source(L1ControllerState.WRWAITDX).event(L1InMessage.MISSC)
                .target(L1ControllerState.MISSC)
                .and().withExternal()
                .source(L1ControllerState.WRWAITDX).event(L1InMessage.MISSD)
                .target(L1ControllerState.MISSD)
                .and().withExternal()
                // End Local
                .source(L1ControllerState.MISSI).event(L1InMessage.CPUWRITE)
                .target(L1ControllerState.WRWAITD)
                .and().withExternal()
                .source(L1ControllerState.WRWAITD).event(L1InMessage.DATA)
                .target(L1ControllerState.WRALLOC)
                .and().withExternal()
                .source(L1ControllerState.WRALLOC).event(L1InMessage.DATA)
                .target(L1ControllerState.HIT)
                .and().withExternal()
                .source(L1ControllerState.MISSC).event(L1InMessage.CPUWRITE)
                .target(L1ControllerState.WRWAITD)//.action(L2CCPURead())
                .and().withExternal()
                .source(L1ControllerState.WRWAITD).event(L1InMessage.DATA)
                .target(L1ControllerState.WRALLOC)
                .and().withExternal()
                .source(L1ControllerState.MISSD).event(L1InMessage.CPUWRITE)
                .target(L1ControllerState.WRWAIT2D)//.action(L1Victimize()).action(L2CCPURead())
                .and().withExternal()
                .source(L1ControllerState.WRWAIT2D).event(L1InMessage.DATA)
                .target(L1ControllerState.WRWAIT1D)
                .and().withExternal()
                .source(L1ControllerState.WRWAIT1D).event(L1InMessage.DATA)
                .target(L1ControllerState.WRALLOC).action(L2CData());
    }

    @Bean
    public Action<L1ControllerState, L1InMessage> L1CPURead() {
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
                        .withPayload(L1InMessage.DATA)
                        .setHeader("source", "L1Data")
                        .setHeader("address", partitionedAddress)
                        .setHeader("data", data)
                        .build();

                // Send successful message back to the controller
                ctx.getStateMachine().sendEvent(responseMessage);

            } else {

                // Check to see if we can pull from Victim Cache before forwarding
                var victimData = l1VictimCache.getData(partitionedAddress);

                if (victimData != null) {
                    var victimCacheMessage = MessageBuilder
                            .withPayload(L1InMessage.DATA)
                            .setHeader("source", "L1Data")
                            .setHeader("address", partitionedAddress)
                            .setHeader("data", victimData)
                            .build();

                    // Send successful message back to the controller
                    ctx.getStateMachine().sendEvent(victimCacheMessage);

                    // Fail out of further error behavior;
                    return;
                }

                // Else we have a MISSI or MISSC
                // Send a miss to transition to miss state
                var missMessage = MessageBuilder
                        .withPayload(L1InMessage.fromControllerState(canRead))
                        .setHeader("source", "L1Data")
                        .build();

                ctx.getStateMachine().sendEvent(missMessage);

                // Then send a read back
                var cpuReadMessage = MessageBuilder
                        .withPayload(L1InMessage.CPUREAD)
                        .setHeader("source", "L1Data")
                        .setHeader("address", partitionedAddress)
                        .setHeader("bytes", bytes)
                        .build();

                ctx.getStateMachine().sendEvent(cpuReadMessage);

                // TODO - what? If MISSD, we need to send the evicted block back to
                if (canRead == DataResponseType.MISSD) {

                    System.out.println("L1C to L1D: Victimize(" + address + ")");

                    var victimizeResponseMessage = MessageBuilder
                            .withPayload(L1InMessage.DATA)
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
    public Action<L1ControllerState, L1InMessage> L1Data() {
        return ctx -> {
            var message = ctx.getMessage();
            var address = message.getHeaders().get("address", String.class);
            var data = (byte[]) message.getHeaders().get("data");

            var l1Address = new Address(address);
            l1Address.componentize(L1_TAG_SIZE, L1_INDEX_SIZE, L1_OFFSET_SIZE);

            var canWrite = level1DataStore.canWriteToCache(l1Address);

            if (canWrite == DataResponseType.HIT) {
                System.out.println("L1C to L1D: Write(" + Arrays.toString(data) + ")");

                var state = ctx.getStateMachine().getState().getId();
                if (L1ControllerState.READ_STATES.contains(state)) {
                    level1DataStore.writeDataToCacheTriggeredByRead(l1Address, data);
                } else if (L1ControllerState.WRITE_STATES.contains(state)) {
                    level1DataStore.writeDataToCache(l1Address, data);
                } else {
                    throw new UnsupportedOperationException("L1D Write Operation received from unrecognized state." + state.toString());
                }

                var responseMessage = MessageBuilder
                        .withPayload(L1InMessage.DATA)
                        .setHeader("source", "L1Data")
                        .setHeader("address", l1Address)
                        .setHeader("data", data)
                        .build();

                // Send successful message back to the controller
                ctx.getStateMachine().sendEvent(responseMessage);
            } else {
                var missMessage = MessageBuilder
                        .withPayload(L1InMessage.fromControllerState(canWrite)) // TODO - Investigate Miss Types
                        .setHeader("source", "L1Data")
                        .build();

                ctx.getStateMachine().sendEvent(missMessage);

                var cpuWriteMessage = MessageBuilder
                        .withPayload(L1InMessage.CPUWRITE)
                        .setHeader("source", "L1Data")
                        .setHeader("address", l1Address)
                        .setHeader("data", data)
                        .build();

                ctx.getStateMachine().sendEvent(cpuWriteMessage);

                // TODO - what? If MISSD, we need to send the evicted block back to
                if (canWrite == DataResponseType.MISSD) {

                    System.out.println("Victimize L1C to L1D (" + address + ")");

                    var victimBlock = l1VictimCache.getData(l1Address);

                    // Send victim data Request Back to L1D with Victimized Data to be replicated to L2C
                    var victimizeResponseMessage = MessageBuilder
                            .withPayload(L1InMessage.DATA)
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
    public Action<L1ControllerState, L1InMessage> L2CCPURead() {
        return ctx -> {
            var message = ctx.getMessage();
            var address = message.getHeaders().get("address", Address.class);
            var bytes = message.getHeaders().get("bytes", Integer.class);
            System.out.println("L1C to L2C: CpuRead(" + address + ")");

            // Place on the L2 Message Queue
            messageBus.enqueueL2Message(message);

            var payload = message.getPayload();

            if (payload == L1InMessage.CPUREAD) {
                // To transition to RDL2WAITD
                var readMessage = MessageBuilder
                        .withPayload(L1InMessage.CPUREAD)
                        .setHeader("source", "L1Data")
                        .setHeader("address", address)
                        .setHeader("bytes", bytes)
                        .build();

                ctx.getStateMachine().sendEvent(readMessage);
            } else if (payload == L1InMessage.CPUWRITE) {
                // Transition to WRWAITD
                var writeMessage = MessageBuilder
                        .withPayload(L1InMessage.CPUREAD)
                        .setHeader("source", "L1Data")
                        .setHeader("address", address)
                        .setHeader("bytes", bytes)
                        .build();

                ctx.getStateMachine().sendEvent(writeMessage);
            }
        };
    }

    @Bean
    public Action<L1ControllerState, L1InMessage> L2CData() {
        return ctx -> {
            var message = ctx.getMessage();
            var data = (byte[]) message.getHeaders().get("data");
            System.out.println("L1C to L2C: Data(" + Arrays.toString(data) + ")");

            messageBus.enqueueL2Message(message);
        };
    }

    @Bean
    public Action<L1ControllerState, L1InMessage> CPUData() {
        return ctx -> {
            var data = (byte[]) ctx.getMessage().getHeaders().get("data");
            System.out.println("L1C to CPU: Data(" + Arrays.toString(data) + ")");

            // report back to the cpu
            cpu.data(data);
        };
    }

    @Bean
    public Action<L1ControllerState, L1InMessage> processL1Message() {
        return ctx -> {
            var message = messageBus.getL1MessageQueue().poll();
            var stateMachine = ctx.getStateMachine();
            var currentState = stateMachine.getState().getId();

            // If we have a message, start processing
            if (message != null && currentState == L1ControllerState.HIT) {
                stateMachine.sendEvent(message);
            }
        };
    }
}