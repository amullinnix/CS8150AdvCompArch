package edu.uno.advcomparch.statemachine;

import edu.uno.advcomparch.controller.Address;
import edu.uno.advcomparch.controller.CacheBlock;
import edu.uno.advcomparch.controller.DataResponseType;
import edu.uno.advcomparch.storage.DynamicRandomAccessMemory;
import edu.uno.advcomparch.storage.Level1WriteBuffer;
import edu.uno.advcomparch.storage.Level2DataStore;
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
@EnableStateMachine(name = "l2ControllerStateMachine")
@AllArgsConstructor
public class L2ControllerStateMachineConfiguration extends StateMachineConfigurerAdapter<ControllerState, ControllerMessage> {

    private static final int L2_TAG_SIZE = 9;
    private static final int L2_INDEX_SIZE = 0;
    private static final int L2_OFFSET_SIZE = 5;

    @Autowired
    StateMachineMessageBus messageBus;

    @Autowired
    Level1WriteBuffer level1WriteBuffer;

    @Autowired
    DynamicRandomAccessMemory memory;

    @Autowired
    Level2DataStore level2DataStore;

    @Override
    public void configure(StateMachineConfigurationConfigurer<ControllerState, ControllerMessage> config) throws Exception {
        config.withConfiguration()
                .autoStartup(false)
                .listener(l2listener());
    }

    @Bean
    public StateMachineListener<ControllerState, ControllerMessage> l2listener() {
        return new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<ControllerState, ControllerMessage> from, State<ControllerState, ControllerMessage> to) {
                System.out.println("State change to " + to.getId());
            }
        };
    }

    @Override
    public void configure(StateMachineStateConfigurer<ControllerState, ControllerMessage> states) throws Exception {
        states.withStates()
                .initial(ControllerState.HIT)
                .end(ControllerState.HIT)
                .state(ControllerState.HIT, processL2Message())
                .state(ControllerState.RDWAITD, L2CPURead())
                .state(ControllerState.RD1WAITD, L2CMemRead()) // Since we can't wait on the event from memory
                .state(ControllerState.RDL2WAITD, L2CMemRead())
                .state(ControllerState.WRWAITDX, L2Data())
                .state(ControllerState.WRALLOC, L2Data())
                .state(ControllerState.WRWAITD, L2CMemRead())
                .state(ControllerState.WRWAIT1D, L2CMemRead()) // Since we can't wait on the event from memory
                .state(ControllerState.MISSC, L2CMemRead())
                .state(ControllerState.MISSD, L2CMemRead())
                .states(EnumSet.allOf(ControllerState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<ControllerState, ControllerMessage> transitions) throws Exception {
        transitions.withExternal()
                .source(ControllerState.HIT).event(ControllerMessage.CPUREAD)
                .target(ControllerState.RDWAITD)
                .and().withExternal()
                .source(ControllerState.RDWAITD).event(ControllerMessage.DATA)
                .target(ControllerState.HIT).action(L2toL1Data())
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
                .event(ControllerMessage.DATA).action(L2Data()).action(L2toL1Data())
                .and().withExternal()
                .source(ControllerState.MISSC).event(ControllerMessage.CPUREAD)
                .target(ControllerState.RDL2WAITD)
                .and().withExternal()
                .source(ControllerState.MISSD).event(ControllerMessage.CPUREAD)
                .target(ControllerState.RD2WAITD)//.action(L2Victimize())
                .and().withExternal()
                .source(ControllerState.RD2WAITD).event(ControllerMessage.DATA)
                .target(ControllerState.RD1WAITD) // no action
                .and().withExternal()
                .source(ControllerState.RD1WAITD).event(ControllerMessage.DATA)
                .target(ControllerState.HIT).action(L2Data()).action(MemData()).action(L2toL1Data())
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
                .target(ControllerState.WRWAITD).action(L2CMemRead())
                .and().withExternal()
                .source(ControllerState.WRWAITD).event(ControllerMessage.DATA)
                .target(ControllerState.WRALLOC)
                .and().withExternal()
                .source(ControllerState.MISSD).event(ControllerMessage.CPUWRITE)
                .target(ControllerState.WRWAIT2D)//.action(L2Victimize()).action(L2CMemRead())
                .and().withExternal()
                .source(ControllerState.WRWAIT2D).event(ControllerMessage.DATA)
                .target(ControllerState.WRWAIT1D)
                .and().withExternal()
                .source(ControllerState.WRWAIT1D).event(ControllerMessage.DATA)
                .target(ControllerState.WRALLOC).action(MemData());
    }

    @Bean
    @Autowired
    public Action<ControllerState, ControllerMessage> L2CPURead() {
        return ctx -> {
            // If queue is non empty, on state transition perform one action.
            var address = ctx.getMessage().getHeaders().get("address", String.class);

            System.out.println("L1C to L2C: CPURead(" + address + ")");

            var partitionedAddress = new Address(address);
            partitionedAddress.componentize(L2_TAG_SIZE, L2_INDEX_SIZE, L2_OFFSET_SIZE);

            var canRead = level2DataStore.isDataPresentInCache(partitionedAddress);
            var data = level2DataStore.getBlockAtAddress(partitionedAddress);

            // If we get nothing back send miss.
            if (canRead == DataResponseType.HIT) {

                var responseMessage = MessageBuilder
                        .withPayload(ControllerMessage.DATA)
                        .setHeader("source", "L2Data")
                        .setHeader("address", address)
                        .setHeader("data", data)
                        .build();

                // Send successful message back to the controller
                ctx.getStateMachine().sendEvent(responseMessage);

            } else {
                // Construct a miss
                var missMessage = MessageBuilder
                        .withPayload(ControllerMessage.fromControllerState(canRead))
                        .setHeader("source", "L2Data")
                        .build();

                ctx.getStateMachine().sendEvent(missMessage);
                // Then send a read back
                var cpuReadMessage = MessageBuilder
                        .withPayload(ControllerMessage.CPUREAD)
                        .setHeader("source", "L2Data")
                        .setHeader("address", address)
                        .build();

                ctx.getStateMachine().sendEvent(cpuReadMessage);

                if (canRead == DataResponseType.MISSD) {

                    System.out.println("L2C to L2D: Victimize(" + address + ")");

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
    public Action<ControllerState, ControllerMessage> L2Data() {
        return ctx -> {
            var message = ctx.getMessage();
            var address = message.getHeaders().get("address", String.class);
            var data = message.getHeaders().get("data", CacheBlock.class);

            var l2Address = new Address(address);
            l2Address.componentize(L2_TAG_SIZE, L2_INDEX_SIZE, L2_OFFSET_SIZE);

            var canWrite = level2DataStore.canWriteToCache(l2Address);

            if (canWrite == DataResponseType.HIT) {
                // TODO - Fetch from write buffer always? Disregard message data?
                var writeBufferData = level1WriteBuffer.getData(l2Address);
                // Clear buffer, mocking synchronous writes
                level1WriteBuffer.getBuffer().clear();

                System.out.println("L2C to L2D: Write(" + Arrays.toString(writeBufferData.getBlock()) + ")");

                level2DataStore.writeDataToCache(l2Address, data);

                var responseMessage = MessageBuilder
                        .withPayload(ControllerMessage.DATA)
                        .setHeader("source", "L2Data")
                        .setHeader("address", l2Address.getAddress())
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
                        .setHeader("address", l2Address.getAddress())
                        .setHeader("data", data)
                        .build();

                ctx.getStateMachine().sendEvent(cpuWriteMessage);

                if (canWrite == DataResponseType.MISSD) {

                    System.out.println("Victimize L1C to L1D (" + address + ")");

                    // TODO - Correct data
                    // Force Data Response
                    var memoryBlock = memory.getMemoryAtAddress(l2Address);

                    // Send victim data Request Back to L1D with Victimized Data to be replicated to L2C
                    var victimizeResponseMessage = MessageBuilder
                            .withPayload(ControllerMessage.DATA)
                            .setHeader("source", "L1Data")
                            .setHeader("address", l2Address.getAddress())
                            .setHeader("data", memoryBlock)
                            .build();

                    ctx.getStateMachine().sendEvent(victimizeResponseMessage);
                }
            }
        };
    }

    @Bean
    public Action<ControllerState, ControllerMessage> L2CMemRead() {
        return ctx -> {
            var message = ctx.getMessage();
            var address = message.getHeaders().get("address", String.class);

            System.out.println("L2C to Mem: CpuRead(" + address + ")");

            var partitionedAddress = new Address(address);
            partitionedAddress.componentize(L2_TAG_SIZE, L2_INDEX_SIZE, L2_OFFSET_SIZE);

            // Fetch from memory
            memory.getMemoryAtAddress(partitionedAddress);

            var payload = message.getPayload();

            if (payload == ControllerMessage.CPUREAD) {
                // To transition to RDL2WAITD
                var readMessage = MessageBuilder
                        .withPayload(ControllerMessage.CPUREAD)
                        .setHeader("source", "L1Data")
                        .setHeader("address", address)
                        .build();

                ctx.getStateMachine().sendEvent(readMessage);
            } else if (payload == ControllerMessage.CPUWRITE) {
                // Transition to WRWAITD
                var writeMessage = MessageBuilder
                        .withPayload(ControllerMessage.CPUWRITE)
                        .setHeader("source", "L1Data")
                        .setHeader("address", address)
                        .build();

                ctx.getStateMachine().sendEvent(writeMessage);
            }

            var memoryBlock = memory.getMemoryAtAddress(partitionedAddress);

            // Need to fetch from memory, then send data request back
            var memResponseMessage = MessageBuilder
                    .withPayload(ControllerMessage.DATA)
                    .setHeader("source", "Mem")
                    .setHeader("address",address)
                    .setHeader("data", memoryBlock)
                    .build();

            ctx.getStateMachine().sendEvent(memResponseMessage);
        };
    }

    @Bean
    public Action<ControllerState, ControllerMessage> MemData() {
        return ctx -> {
            var message = ctx.getMessage();
            var cacheBlock = message.getHeaders().get("data", CacheBlock.class);
            var address = message.getHeaders().get("address", String.class);
            System.out.println("L2C to Mem: Data(" + Arrays.toString(cacheBlock.getBlock()) + ")");

            var l2address = new Address(address);
            l2address.componentize(L2_TAG_SIZE, L2_INDEX_SIZE, L2_OFFSET_SIZE);

            memory.writeToRam(l2address, cacheBlock);
        };
    }

    @Bean
    public Action<ControllerState, ControllerMessage> L2toL1Data() {
        return ctx -> {
            var message = ctx.getMessage();
            var cacheBlock = ctx.getMessage().getHeaders().get("data", CacheBlock.class);

            System.out.println("L2C to L1: Data(" + Arrays.toString(cacheBlock.getBlock()) + ")");

            // report back to the L1 Controller
            messageBus.enqueueL1Message(message);
        };
    }

    @Bean
    public Action<ControllerState, ControllerMessage> processL2Message() {
        return ctx -> {
            var message = messageBus.getL2MessageQueue().poll();
            var stateMachine = ctx.getStateMachine();
            var currentState = stateMachine.getState().getId();

            // If we have a message, start processing
            if (message != null && currentState == ControllerState.HIT) {
                stateMachine.sendEvent(message);
            }
        };
    }
}