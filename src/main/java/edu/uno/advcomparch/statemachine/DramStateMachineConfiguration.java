package edu.uno.advcomparch.statemachine;


import edu.uno.advcomparch.storage.DynamicRandomAccessMemory;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.state.State;

@Configuration
@EnableStateMachine(name = "dramStateMachine")
@AllArgsConstructor
public class DramStateMachineConfiguration extends StateMachineConfigurerAdapter<ControllerState, ControllerMessage> {

    @Autowired
    StateMachineMessageBus messageBus;

    @Autowired
    DynamicRandomAccessMemory memory;

    @Override
    public void configure(StateMachineConfigurationConfigurer<ControllerState, ControllerMessage> config) throws Exception {
        config.withConfiguration()
                .autoStartup(false)
                .listener(dramListener());
    }

    @Bean
    public StateMachineListener<ControllerState, ControllerMessage> dramListener() {
        return new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<ControllerState, ControllerMessage> from, State<ControllerState, ControllerMessage> to) {
                if(from == null) {
                    System.out.println("DRAM State change to " + to.getId());
                } else {
                    System.out.println("DRAM State change from " + from.getId() + " to " + to.getId());
                }
            }
        };
    }

    @Override
    public void configure(StateMachineStateConfigurer<ControllerState, ControllerMessage> states) throws Exception {
        states.withStates()
                .initial(ControllerState.HIT)
                .end(ControllerState.HIT)
                .state(ControllerState.HIT, processMessage());
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<ControllerState, ControllerMessage> transitions) throws Exception {
        transitions.withExternal()
                .source(ControllerState.HIT).event(ControllerMessage.CPUREAD)
                .target(ControllerState.HIT);
    }

    @Bean
    public Action<ControllerState, ControllerMessage> processMessage() {
        return context -> {
            System.out.println("DRAM: processing message");
            var message = messageBus.getDramQueue().poll();
            var stateMachine = context.getStateMachine();
            var currentState = stateMachine.getState().getId();

            if(message != null && currentState == ControllerState.HIT) {
                System.out.println("Message received on DRAM, sending event: ");
                stateMachine.sendEvent(message);
            } else {
                System.out.println("I don't know what to do here? Take a nap?");
            }
        };
    }
}
