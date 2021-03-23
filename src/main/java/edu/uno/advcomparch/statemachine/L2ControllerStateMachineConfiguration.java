package edu.uno.advcomparch.statemachine;

import edu.uno.advcomparch.controller.Level1Controller;
import edu.uno.advcomparch.repository.DataRepository;
import edu.uno.advcomparch.repository.DataResponseType;
import edu.uno.advcomparch.storage.DynamicRandomAccessMemory;
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

import java.util.EnumSet;

@Configuration
@EnableStateMachine
@AllArgsConstructor
// TODO - Manage Notion of Cycle (Possibly externally). Manage Output Message building
public class L2ControllerStateMachineConfiguration extends StateMachineConfigurerAdapter<L1ControllerState, L1InMessage> {

    @Autowired
    Level1Controller level1Controller;

    @Autowired
    DataRepository<String, String> l2DataRepository;

    @Autowired
    DynamicRandomAccessMemory<String> dynamicRandomAccessMemory;

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
//                .state(L1ControllerState.RDWAITD, L1CPURead())
//                .state(L1ControllerState.MISSI, L2CCPURead())
//                .state(L1ControllerState.MISSC, L2CCPURead())
//                .state(L1ControllerState.MISSD, L2CCPURead())
                .states(EnumSet.allOf(L1ControllerState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<L1ControllerState, L1InMessage> transitions) throws Exception {
        transitions.withExternal()
                .source(L1ControllerState.START).event(L1InMessage.START)
                .target(L1ControllerState.HIT)//.action(initAction())
                .and().withExternal()
                .source(L1ControllerState.HIT).event(L1InMessage.CPUREAD)
                .target(L1ControllerState.RDWAITD)
                .and().withExternal()
                .source(L1ControllerState.RDWAITD).event(L1InMessage.DATA)
                .target(L1ControllerState.HIT)//.action(CPUData())
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
                .source(L1ControllerState.MISSI).event(L1InMessage.CPUREAD)
                .target(L1ControllerState.RDL2WAITD)
                .and().withExternal()
                .source(L1ControllerState.RDL2WAITD).target(L1ControllerState.HIT)
                .event(L1InMessage.DATA).action(L2Data())//.action(CPUData())
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
                .target(L1ControllerState.HIT).action(L2Data())//.action(L2CData()).action(CPUData())
                .and().withExternal()
                .source(L1ControllerState.HIT).event(L1InMessage.CPUWRITE)
                .target(L1ControllerState.HIT).action(L2Data())
                .and().withExternal()
                .source(L1ControllerState.MISSI).event(L1InMessage.CPUWRITE)
                .target(L1ControllerState.WRWAITD)//.action(L2CCPURead())
                .and().withExternal()
                .source(L1ControllerState.WRWAITD).event(L1InMessage.DATA)
                .target(L1ControllerState.WRALLOC).action(L2Data())
                .and().withExternal()
                .source(L1ControllerState.WRALLOC).event(L1InMessage.DATA)
                .target(L1ControllerState.HIT).action(L2Data())
                .and().withExternal()
                .source(L1ControllerState.MISSC).event(L1InMessage.CPUWRITE)
                .target(L1ControllerState.WRWAITD)//.action(L2CCPURead())
                .and().withExternal()
                .source(L1ControllerState.WRWAITD).event(L1InMessage.DATA)
                .target(L1ControllerState.WRALLOC).action(L2Data())
                .and().withExternal()
                .source(L1ControllerState.MISSD).event(L1InMessage.CPUWRITE)
                .target(L1ControllerState.WRWAIT2D).action(L2Victimize())//.action(L2CCPURead())
                .and().withExternal()
                .source(L1ControllerState.WRWAIT2D).event(L1InMessage.DATA)
                .target(L1ControllerState.WRWAITD1D)
                .and().withExternal()
                .source(L1ControllerState.WRWAITD1D).event(L1InMessage.DATA)
                .target(L1ControllerState.WRALLOC).action(L2Data());//.action(L2CData());
    }

    @Bean
    @Autowired
    public Action<L1ControllerState, L1InMessage> L2CPURead() {
        return ctx -> {
            // If queue is non empty, on state transition perform one action.
            var data = ctx.getMessage().getHeaders().get("data", String.class);

            System.out.println("CPU to L1C: CPURead(" + data + ")");

            var response = l2DataRepository.get(data);
            var responseType = response.getType();

            // if we get nothing back send miss.
            if (responseType != DataResponseType.HIT) {

                // construct a miss
                var missMessage = MessageBuilder
                        .withPayload(L1InMessage.fromDataResponseType(responseType))
                        .setHeader("source", "L1Data")
                        .build();

                ctx.getStateMachine().sendEvent(missMessage);
            } else {
                var responseMessage = MessageBuilder
                        .withPayload(L1InMessage.DATA)
                        .setHeader("source", "L1Data")
                        .setHeader("data", response)
                        .build();

                // Send Data back to CPU if included in similar step
                level1Controller.enqueueMessage(data);

                ctx.getStateMachine().sendEvent(responseMessage);
            }
        };
    }

    @Bean
    public Action<L1ControllerState, L1InMessage> L2Data() {
        return ctx -> {
//            var data = ctx.getMessage().getHeaders().get("data", DataResponse.class).getData();
            var data = ctx.getMessage().getHeaders().get("data", String.class);
            System.out.println("L1C to L1D: Data(" + data + ")");

            l2DataRepository.write(data);
        };
    }

    @Bean
    public Action<L1ControllerState, L1InMessage> L2Victimize() {
        return ctx -> {
            var data = ctx.getMessage().getHeaders().get("data", String.class);
            System.out.println("L1C to L1D: Victimize(" + data + ")");

            l2DataRepository.victimize(data);
        };
    }

}