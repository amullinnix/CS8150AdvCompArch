package edu.uno.advcomparch.statemachine;

import edu.uno.advcomparch.controller.Address;
import edu.uno.advcomparch.controller.DataResponseType;
import edu.uno.advcomparch.controller.Level1Controller;
import edu.uno.advcomparch.repository.DataRepository;
import edu.uno.advcomparch.storage.DynamicRandomAccessMemory;
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
public class L2ControllerStateMachineConfiguration extends StateMachineConfigurerAdapter<L1ControllerState, L1InMessage> {

    @Autowired
    Level1Controller level1Controller;

    @Autowired
    DynamicRandomAccessMemory<String> memory;

    @Autowired
    DataRepository<String, String> l2DataRepository;

    @Autowired
    Level2DataStore level2DataStore;

    @Override
    public void configure(StateMachineConfigurationConfigurer<L1ControllerState, L1InMessage> config) throws Exception {
        config.withConfiguration()
                .autoStartup(false)
                .listener(l2listener());
    }

    @Bean
    public StateMachineListener<L1ControllerState, L1InMessage> l2listener() {
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
                .state(L1ControllerState.RDWAITD, L2CPURead())
                .state(L1ControllerState.RDL2WAITD, L2CMemRead())
                .state(L1ControllerState.RD1WAITD, propagateL2Data())
                .state(L1ControllerState.WRWAITDX, L2Data())
                .state(L1ControllerState.WRALLOC, L2Data())
                .state(L1ControllerState.WRWAIT1D, propagateL2Data())
                .state(L1ControllerState.WRWAITD, L2CMemRead())
                .state(L1ControllerState.MISSC, L2CMemRead())
                .state(L1ControllerState.MISSD, L2CMemRead())
                .states(EnumSet.allOf(L1ControllerState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<L1ControllerState, L1InMessage> transitions) throws Exception {
        transitions.withExternal()
                .source(L1ControllerState.HIT).event(L1InMessage.CPUREAD)
                .target(L1ControllerState.RDWAITD)
                .and().withExternal()
                .source(L1ControllerState.RDWAITD).event(L1InMessage.DATA)
                .target(L1ControllerState.HIT).action(L2toL1Data())
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
                .event(L1InMessage.DATA).action(L2Data()).action(L2toL1Data())
                .and().withExternal()
                .source(L1ControllerState.MISSC).event(L1InMessage.CPUREAD)
                .target(L1ControllerState.RDL2WAITD)
                .and().withExternal()
                .source(L1ControllerState.MISSD).event(L1InMessage.CPUREAD)
                .target(L1ControllerState.RD2WAITD).action(L2Victimize())
                .and().withExternal()
                .source(L1ControllerState.RD2WAITD).event(L1InMessage.DATA)
                .target(L1ControllerState.RD1WAITD) // no action
                .and().withExternal()
                .source(L1ControllerState.RD1WAITD).event(L1InMessage.DATA)
                .target(L1ControllerState.HIT).action(L2Data()).action(MemData()).action(L2toL1Data())
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
                .target(L1ControllerState.WRWAITD).action(L2CMemRead())
                .and().withExternal()
                .source(L1ControllerState.WRWAITD).event(L1InMessage.DATA)
                .target(L1ControllerState.WRALLOC)
                .and().withExternal()
                .source(L1ControllerState.MISSD).event(L1InMessage.CPUWRITE)
                .target(L1ControllerState.WRWAIT2D).action(L2Victimize()).action(L2CMemRead())
                .and().withExternal()
                .source(L1ControllerState.WRWAIT2D).event(L1InMessage.DATA)
                .target(L1ControllerState.WRWAIT1D)
                .and().withExternal()
                .source(L1ControllerState.WRWAIT1D).event(L1InMessage.DATA)
                .target(L1ControllerState.WRALLOC).action(MemData());
    }

    @Bean
    @Autowired
    public Action<L1ControllerState, L1InMessage> L2CPURead() {
        return ctx -> {
            // If queue is non empty, on state transition perform one action.
            var address = ctx.getMessage().getHeaders().get("address", String.class);
            var bytes = ctx.getMessage().getHeaders().get("bytes", Integer.class);

            System.out.println("CPU to L1C: CPURead(" + address + ")");


            var partitionedAddress = new Address(address);
            var canRead = level2DataStore.isDataPresentInCache(partitionedAddress);

            // If we get nothing back send miss.
            if (canRead == DataResponseType.HIT) {
                var data = level2DataStore.getBlockAtAddress(partitionedAddress);

                var responseMessage = MessageBuilder
                        .withPayload(L1InMessage.DATA)
                        .setHeader("source", "L2Data")
                        .setHeader("address", address)
                        .setHeader("data", data)
                        .build();

                // Send successful message back to the controller
                ctx.getStateMachine().sendEvent(responseMessage);

            } else {
                // Construct a miss
                var missMessage = MessageBuilder
                        .withPayload(L1InMessage.fromControllerState(canRead))
                        .setHeader("source", "L1Data")
                        .build();

                ctx.getStateMachine().sendEvent(missMessage);
                // Then send a read back
                var cpuReadMessage = MessageBuilder
                        .withPayload(L1InMessage.CPUREAD)
                        .setHeader("source", "L1Data")
                        .setHeader("address", address)
                        .setHeader("bytes", bytes)
                        .build();

                ctx.getStateMachine().sendEvent(cpuReadMessage);
            }
        };
    }

    @Bean
    public Action<L1ControllerState, L1InMessage> L2Data() {
        return ctx -> {
            var message = ctx.getMessage();
            var address = message.getHeaders().get("address", Address.class);
            var data = (byte[]) message.getHeaders().get("data");

            var canWrite = level2DataStore.canWriteToCache(address);

            if (canWrite == DataResponseType.HIT) {
                System.out.println("L1C to L1D: Write(" + Arrays.toString(data) + ")");

                level2DataStore.writeDataToCache(address, data);

                var responseMessage = MessageBuilder
                        .withPayload(L1InMessage.DATA)
                        .setHeader("source", "L1Data")
                        .setHeader("address", new Address("101", "010", "101"))
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
                        .setHeader("address", new Address("101", "010", "101"))
                        .setHeader("data", data)
                        .build();

                ctx.getStateMachine().sendEvent(cpuWriteMessage);
            }
        };
    }

    @Bean
    public Action<L1ControllerState, L1InMessage> L2Victimize() {
        return ctx -> {
            var address = ctx.getMessage().getHeaders().get("address", Address.class);
            System.out.println("L1C to L1D: Victimize(" + address + ")");

            l2DataRepository.victimize(address);
        };
    }

    @Bean
    public Action<L1ControllerState, L1InMessage> L2CMemRead() {
        return ctx -> {
            var message = ctx.getMessage();
            var address = message.getHeaders().get("address", Address.class);
            var bytes = message.getHeaders().get("bytes", Integer.class);
            System.out.println("L2C to Mem: CpuRead(" + address + ")");

            // Look at actual queueing of messages
            memory.getMemoryAtAddress(address);

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
    public Action<L1ControllerState, L1InMessage> MemData() {
        return ctx -> {
            var message = ctx.getMessage();
            var data = (byte[]) message.getHeaders().get("data");
            System.out.println("L1C to L2C: Data(" + Arrays.toString(data) + ")");

            // TODO - move this to a state machine.
            memory.setMemory(data);
        };
    }

    @Bean
    public Action<L1ControllerState, L1InMessage> L2toL1Data() {
        return ctx -> {
            var message = ctx.getMessage();
            var data = (byte[]) ctx.getMessage().getHeaders().get("data");
            System.out.println("L2C to CPU: Data(" + Arrays.toString(data) + ")");

            // report back to the L1 Controller
            level1Controller.enqueueMessage(message);

            // We've already passed this two the CPU in L1Read, maybe we need to move it back out here.
            System.out.println(data);
        };
    }

    @Bean
    public Action<L1ControllerState, L1InMessage> propagateL2Data() {
        return ctx -> {
            // Propagate RD2WAITD Data --> RD1WaitD
            var message = ctx.getMessage();
            ctx.getStateMachine().sendEvent(message);
        };
    }

    @Bean
    public Action<L1ControllerState, L1InMessage> l2InitAction() {
        return ctx -> System.out.println(ctx.getTarget().getId());
    }
}