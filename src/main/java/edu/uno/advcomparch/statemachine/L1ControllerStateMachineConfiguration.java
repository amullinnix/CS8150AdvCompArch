package edu.uno.advcomparch.statemachine;

import edu.uno.advcomparch.controller.Address;
import edu.uno.advcomparch.controller.DataResponseType;
import edu.uno.advcomparch.controller.Level1Controller;
import edu.uno.advcomparch.controller.Level2Controller;
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
    Level1Controller level1Controller;

    @Autowired
    Level2Controller level2Controller;

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

                // After each stage in the process instruction
                level1Controller.processMessage();
            }
        };
    }

    @Override
    public void configure(StateMachineStateConfigurer<L1ControllerState, L1InMessage> states) throws Exception {
        states.withStates()
                .initial(L1ControllerState.HIT)
                .end(L1ControllerState.HIT)
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
                .target(L1ControllerState.RD2WAITD).action(L1Victimize())
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
                .target(L1ControllerState.WRWAITD).action(L2CCPURead())
                .and().withExternal()
                .source(L1ControllerState.WRWAITD).event(L1InMessage.DATA)
                .target(L1ControllerState.WRALLOC)
                .and().withExternal()
                .source(L1ControllerState.MISSD).event(L1InMessage.CPUWRITE)
                .target(L1ControllerState.WRWAIT2D).action(L1Victimize()).action(L2CCPURead())
                .and().withExternal()
                .source(L1ControllerState.WRWAIT2D).event(L1InMessage.DATA)
                .target(L1ControllerState.WRWAIT1D)
                .and().withExternal()
                .source(L1ControllerState.WRWAIT1D).event(L1InMessage.DATA)
                .target(L1ControllerState.WRALLOC).action(L2CData());
    }

    @Bean
    @Autowired
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
                var victimData = l1VictimCache.getData(partitionedAddress, bytes);

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
//                    CacheBlock evictedBlock;
//
//                    if(evictedBlock != null) {
//                        this.level1DataStore.get(evictedBlock);
//                    }

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

            // TODO why single byte write?
//            var evictedBlock = level1DataStore.writeDataToCache(l1Address, data);

            if (canWrite == DataResponseType.HIT) {
                System.out.println("L1C to L1D: Write(" + Arrays.toString(data) + ")");

                level1DataStore.writeDataToCache(l1Address, data);

                // Need to identify resolvable states;
//                var state = ctx.getStateMachine().getState().getId();
//
//                if (state == L1ControllerState.RDL2WAITD) {
//                    level1DataStore.writeDataToCacheTriggeredByRead(l1Address, data);
//                } else if (state == L1ControllerState.WRWAIT2D) {
//                    level1DataStore.writeDataToCache(l1Address, data);
//                } else {
//                    throw new UnsupportedOperationException("L1D Write Operation received from unrecognized message type.");
//                }

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
//                    CacheBlock evictedBlock;

//                     Why even bother breaking this out.
//                    if(evictedBlock != null) {
//                        this.level1DataStore.get(evictedBlock);
//                    }
                    // Send Data Request Back to L1D with Victimized Data to be replicated to L2C
                    var victimizeResponseMessage = MessageBuilder
                            .withPayload(L1InMessage.DATA)
                            .setHeader("source", "L1Data")
                            .setHeader("address", l1Address)
                            .setHeader("data", data)
                            .build();

                    ctx.getStateMachine().sendEvent(victimizeResponseMessage);

                }
            }
        };
    }

    @Bean
    // TODO - Missing L1DataStoreInterface
    public Action<L1ControllerState, L1InMessage> L1Victimize() {
        return ctx -> {
            var address = ctx.getMessage().getHeaders().get("address", Address.class);
            System.out.println("L1C to L1D: Victimize(" + address + ")");

            // Why not victimize a specific method
//            CacheBlock evictedBlock = level1DataStore.writeDataToCache(address, b);
//
//            if(evictedBlock != null) {
//                level1DataStore.victimCache.getCache().add(evictedBlock);
//            }
        };
    }

    @Bean
    public Action<L1ControllerState, L1InMessage> L2CCPURead() {
        return ctx -> {
            var message = ctx.getMessage();
            var address = message.getHeaders().get("address", Address.class);
            var bytes = message.getHeaders().get("bytes", Integer.class);
            System.out.println("L1C to L2C: CpuRead(" + address + ")");

            // TODO - Look at actual queueing of messages
            level2Controller.enqueueMessage(message);
            // Or send the message -> might have to build an overarching machine
//            l2ControllerStateMachine.sendEvent(message);

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

            // TODO - Look at actual queueing of messages
            level2Controller.enqueueMessage(message);
            // Or send the message
            // l2ControllerStateMachine.sendEvent(message);
        };
    }

    @Bean
    public Action<L1ControllerState, L1InMessage> CPUData() {
        return ctx -> {
            var data = (byte[]) ctx.getMessage().getHeaders().get("data");
            System.out.println("L1C to CPU: Data(" + Arrays.toString(data) + ")");

            // report back to the cpu
            cpu.data(data);

            // We've already passed this two the CPU in L1Read, maybe we need to move it back out here.
            System.out.println(data);
        };
    }
}