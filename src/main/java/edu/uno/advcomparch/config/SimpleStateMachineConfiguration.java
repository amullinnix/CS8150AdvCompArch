package edu.uno.advcomparch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;

import java.util.Arrays;
import java.util.HashSet;

@Configuration
@EnableStateMachine
public class SimpleStateMachineConfiguration extends StateMachineConfigurerAdapter<String, String> {

    @Override
    public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
        states.withStates()
                .initial("SI")
                .end("SF")
                .states(new HashSet<>(Arrays.asList("S1", "S2", "S3")))
                .stateEntry("S3", entryAction())
                .stateDo("S3", executeAction(), errorAction())
                .stateExit("S3", exitAction());
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
        transitions.withExternal()
                .source("SI").target("S1").event("E1").action(initAction()).and()
                .withExternal()
                .source("S1").target("S2").event("E2").and()
                .withExternal()
                .source("S2").target("SF").event("end");
    }

    @Bean
    public Action<String, String> initAction() {
        return ctx -> System.out.println(ctx.getTarget().getId());
    }

    @Bean
    public Action<String, String> executeAction() {
        return ctx -> System.out.println("Do" + ctx.getTarget().getId());
    }

    @Bean
    public Action<String, String> errorAction() {
        return ctx -> System.out.println(
                "Error " + ctx.getSource().getId() + ctx.getException());
    }

    @Bean
    public Action<String, String> entryAction() {
        return ctx -> System.out.println(
                "Entry " + ctx.getTarget().getId());
    }

    @Bean
    public Action<String, String> exitAction() {
        return ctx -> System.out.println(
                "Exit " + ctx.getSource().getId() + " -> " + ctx.getTarget().getId());
    }

    @Bean
    public Guard<String, String> simpleGuard() {
        return ctx -> (int) ctx.getExtendedState()
                .getVariables()
                .getOrDefault("approvalCount", 0) > 0;
    }
}