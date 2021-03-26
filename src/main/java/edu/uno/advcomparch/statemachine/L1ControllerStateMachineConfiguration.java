package edu.uno.advcomparch.statemachine;

import edu.uno.advcomparch.controller.Address;
import edu.uno.advcomparch.controller.ControllerState;
import edu.uno.advcomparch.controller.Level1Controller;
import edu.uno.advcomparch.controller.Level2Controller;
import edu.uno.advcomparch.cpu.CentralProcessingUnit;
import edu.uno.advcomparch.repository.DataRepository;
import edu.uno.advcomparch.storage.Level1DataStore;
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
@EnableStateMachine
@AllArgsConstructor
// TODO - Manage Notion of Cycle (Possibly externally). Manage Output Message building
public class L1ControllerStateMachineConfiguration extends StateMachineConfigurerAdapter<L1ControllerState, L1InMessage> {

    @Autowired
    Level1Controller level1Controller;

    @Autowired
    Level2Controller level2Controller;

    @Autowired
    CentralProcessingUnit<String> cpu;

    @Autowired
    DataRepository<String, String> l1DataRepository;

    @Autowired
    Level1DataStore level1DataStore;

    @Override
    public void configure(StateMachineConfigurationConfigurer<L1ControllerState, L1InMessage> config) throws Exception {
        config.withConfiguration()
                .autoStartup(false)
                .listener(listener());
    }

    @Bean
    public StateMachineListener<L1ControllerState, L1InMessage> listener() {
        return new StateMachineListenerAdapter<L1ControllerState, L1InMessage>() {

            // can use on parent state machine to iterate clock
            @Override
            public void stateChanged(State<L1ControllerState, L1InMessage> from, State<L1ControllerState, L1InMessage> to) {
                System.out.println("State change to " + to.getId());

                // After each stage in the process instruction
                level1Controller.processInstruction();
            }
        };
    }

    @Override
    public void configure(StateMachineStateConfigurer<L1ControllerState, L1InMessage> states) throws Exception {
        states.withStates()
                .initial(L1ControllerState.START)
                .end(L1ControllerState.END)
                .state(L1ControllerState.RDWAITD, L1CPURead())
                .state(L1ControllerState.WRWAITDX, L1Data())
                .state(L1ControllerState.WRALLOC, L1Data()) // TODO - Why two data messages to go from wrwait - hit
//                .state(L1ControllerState.MISSI, L2CCPURead())
                .state(L1ControllerState.RDL2WAITD, L2CCPURead())
                .state(L1ControllerState.WRWAITD, L2CCPURead())
                .state(L1ControllerState.MISSC, L2CCPURead())
                .state(L1ControllerState.MISSD, L2CCPURead())
                .states(EnumSet.allOf(L1ControllerState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<L1ControllerState, L1InMessage> transitions) throws Exception {
        transitions.withExternal()
                .source(L1ControllerState.START).event(L1InMessage.START)
                .target(L1ControllerState.HIT).action(initAction())
                .and().withExternal()
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
//                .source(L1ControllerState.HIT).event(L1InMessage.CPUWRITE)
//                .target(L1ControllerState.HIT).action(L1Data())
//                .and().withExternal()
                .source(L1ControllerState.MISSI).event(L1InMessage.CPUWRITE)
                .target(L1ControllerState.WRWAITD)
                .and().withExternal()
                .source(L1ControllerState.WRWAITD).event(L1InMessage.DATA)
                .target(L1ControllerState.WRALLOC)
                .and().withExternal()
                .source(L1ControllerState.WRALLOC).event(L1InMessage.DATA)
                .target(L1ControllerState.HIT).action(L1Data())
                .and().withExternal()
                .source(L1ControllerState.MISSC).event(L1InMessage.CPUWRITE)
                .target(L1ControllerState.WRWAITD).action(L2CCPURead())
                .and().withExternal()
                .source(L1ControllerState.WRWAITD).event(L1InMessage.DATA)
                .target(L1ControllerState.WRALLOC).action(L1Data())
                .and().withExternal()
                .source(L1ControllerState.MISSD).event(L1InMessage.CPUWRITE)
                .target(L1ControllerState.WRWAIT2D).action(L1Victimize()).action(L2CCPURead())
                .and().withExternal()
                .source(L1ControllerState.WRWAIT2D).event(L1InMessage.DATA)
                .target(L1ControllerState.WRWAITD1D)
                .and().withExternal()
                .source(L1ControllerState.WRWAITD1D).event(L1InMessage.DATA)
                .target(L1ControllerState.WRALLOC).action(L1Data()).action(L2CData());
    }

    @Bean
    @Autowired
    public Action<L1ControllerState, L1InMessage> L1CPURead() {
        return ctx -> {
            // If queue is non empty, on state transition perform one action.
            var address = ctx.getMessage().getHeaders().get("address", Address.class);
            var bytes = ctx.getMessage().getHeaders().get("bytes", Integer.class);

            System.out.println("CPU to L1C: CPURead(" + address + ")");

            var canRead = level1DataStore.isDataPresentInCache(address);

            // TODO Derive missing types, MISSC, MISSI, MISSD
//            var responseType = response.getType();

            // if we get nothing back send miss.
            if (canRead != ControllerState.HIT) {
                // construct a miss
                var missMessage = MessageBuilder
//                        .withPayload(L1InMessage.fromDataResponseType(responseType))
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

            } else {
                var data = level1DataStore.getDataAtAddress(address, bytes);

                var responseMessage = MessageBuilder
                        .withPayload(L1InMessage.DATA)
                        .setHeader("source", "L1Data")
                        .setHeader("data", data)
                        .build();

                // Send Data back to CPU if included in similar step
                // cpu.data(data); - or do we send this as a transition event

                // Send successful message back to the controller
                ctx.getStateMachine().sendEvent(responseMessage);
            }
        };
    }

    @Bean
    public Action<L1ControllerState, L1InMessage> L1Data() {
        return ctx -> {
            var message = ctx.getMessage();
            var address = message.getHeaders().get("address", Address.class);
            var data = (byte[]) message.getHeaders().get("data");

            var canWrite = level1DataStore.canWriteToCache(address);

            if (canWrite == ControllerState.HIT) {
                System.out.println("L1C to L1D: Write(" + Arrays.toString(data) + ")");

                level1DataStore.writeDataToCache(address, data);

                var responseMessage = MessageBuilder
                        .withPayload(L1InMessage.DATA)
                        .setHeader("source", "L1Data")
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
    // TODO - need to sort out still
    public Action<L1ControllerState, L1InMessage> L1Victimize() {
        return ctx -> {
            var data = ctx.getMessage().getHeaders().get("data", String.class);
            System.out.println("L1C to L1D: Victimize(" + data + ")");

            l1DataRepository.victimize(data);
        };
    }

    @Bean
    public Action<L1ControllerState, L1InMessage> L2CCPURead() {
        return ctx -> {
            var message = ctx.getMessage();
            var address = message.getHeaders().get("address", Address.class);
            var bytes = message.getHeaders().get("bytes", Integer.class);
            System.out.println("L1C to L2C: CpuRead(" + address + ")");

            // Look at actual queueing of messages
            level2Controller.enqueueMessage(message);

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

            // TODO - move this to a state machine.
            level2Controller.enqueueMessage(message);
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

    @Bean
    public Action<L1ControllerState, L1InMessage> initAction() {
        return ctx -> System.out.println(ctx.getTarget().getId());
    }
}