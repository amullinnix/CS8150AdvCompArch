package edu.uno.advcomparch.statemachine;

import edu.uno.advcomparch.controller.Level1Controller;
import edu.uno.advcomparch.controller.Level2Controller;
import edu.uno.advcomparch.cpu.CentralProcessingUnit;
import edu.uno.advcomparch.instruction.Message;
import edu.uno.advcomparch.repository.DataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                .states(EnumSet.allOf(L1ControllerState.class))
                .stateEntry(L1ControllerState.HIT, entryAction())
                .stateDo(L1ControllerState.MISSC, executeAction(), errorAction())
                .stateExit(L1ControllerState.MISSI, exitAction());
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<L1ControllerState, L1InMessage> transitions) throws Exception {
        transitions.withExternal()
                .source(L1ControllerState.START).event(L1InMessage.START)
                .target(L1ControllerState.HIT).action(initAction())
                .and().withExternal()
                .source(L1ControllerState.HIT).event(L1InMessage.CPUREAD)
                .target(L1ControllerState.RDWAITD).action(L1CPURead())
                .and().withExternal()
                .source(L1ControllerState.RDWAITD).event(L1InMessage.DATA)
                .target(L1ControllerState.HIT).action(CPUData())
                .and().withExternal()
                .source(L1ControllerState.MISSI).event(L1InMessage.CPUREAD)
                .target(L1ControllerState.RDL2WAITD).action(L2CCPURead())
                .and().withExternal()
                .source(L1ControllerState.RDL2WAITD).target(L1ControllerState.HIT)
                .event(L1InMessage.DATA).action(L1Data())
                .and().withExternal()
                .source(L1ControllerState.MISSC).event(L1InMessage.CPUREAD)
                .target(L1ControllerState.RDL2WAITD).action(L2CCPURead())
                .and().withExternal()
                .source(L1ControllerState.MISSD).event(L1InMessage.CPUREAD)
                .target(L1ControllerState.RD2WAITD).action(L1Victimize()).action(L2CCPURead())
                .and().withExternal()
                .source(L1ControllerState.RD2WAITD).event(L1InMessage.DATA)
                .target(L1ControllerState.RD1WAITD) // no action
                .and().withExternal()
                .source(L1ControllerState.RD1WAITD).event(L1InMessage.DATA)
                .target(L1ControllerState.HIT).action(L1Data()).action(L2CData()).action(CPUData())
                .and().withExternal()
                .source(L1ControllerState.HIT).event(L1InMessage.CPUWRITE)
                .target(L1ControllerState.HIT).action(L1Data())
                .and().withExternal()
                .source(L1ControllerState.MISSI).event(L1InMessage.CPUWRITE)
                .target(L1ControllerState.WRWAITD).action(L2CCPURead())
                .and().withExternal()
                .source(L1ControllerState.WRWAITD).event(L1InMessage.DATA)
                .target(L1ControllerState.WRALLOC).action(L1Data())
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
    // Parse the message, build output, queue instruction
    public Action<L1ControllerState, L1InMessage> L1CPURead() {
        return ctx -> {
            // TODO - Enqueue messages, but might consider processing as well, could be done in listener
            // If queue is non empty, on state transition perform one action.
            var readMessage = ctx.getExtendedState().get("message", Message.class);

            System.out.println(readMessage.toString());

            // Current behavior dictates this be handled by the controller
             level1Controller.enqueueMessage(readMessage);
        };
    }

    @Bean
    public Action<L1ControllerState, L1InMessage> L1Data() {
        return ctx -> {
            var message = ctx.getExtendedState().get("message", Message.class);
            var address = message.getInstruction().getAddress();

            System.out.println(message.toString());

            // TODO could pass instruction or fetch
            l1DataRepository.get(address);

        };
    }

    @Bean
    public Action<L1ControllerState, L1InMessage> L1Victimize() {
        return ctx -> {
            var message = ctx.getExtendedState().get("message", Message.class);
            var address = message.getInstruction().getAddress();

            System.out.println(message.toString());

            l1DataRepository.victimize(address);
        };
    }

    @Bean
    public Action<L1ControllerState, L1InMessage> L2CCPURead() {
        return ctx -> {
            var message = ctx.getExtendedState().get("message", Message.class);

            System.out.println(message.toString());

            level2Controller.enqueueMessage(message);
        };
    }

    @Bean
    public Action<L1ControllerState, L1InMessage> L2CData() {
        return ctx -> {
            var message = ctx.getExtendedState().get("message", Message.class);

            System.out.println(message.toString());

            // TODO - is a data request to L2C actually a write?
            level2Controller.enqueueMessage(message);
        };
    }

    @Bean
    public Action<L1ControllerState, L1InMessage> CPUData() {
        return ctx -> {
            var message = ctx.getExtendedState().get("message", Message.class);
            var data = message.getInstruction().getSource();

            System.out.println(message.toString());

            // TODO
            cpu.data(data);
        };
    }

    @Bean
    public Action<L1ControllerState, L1InMessage> initAction() {
        return ctx -> {
            // populate message from cpu,
            // TODO refine input messages to strip from statemachine headers.
            ctx.getExtendedState()
                    .getVariables().put("message", new Message());
            System.out.println(ctx.getTarget().getId());
        };
    }

    @Bean
    public Action<L1ControllerState, L1InMessage> executeAction() {
        return ctx -> System.out.println("Do" + ctx.getTarget().getId());
    }

    @Bean
    public Action<L1ControllerState, L1InMessage> errorAction() {
        return ctx -> System.out.println(
                "Error " + ctx.getSource().getId() + ctx.getException());
    }

    @Bean
    public Action<L1ControllerState, L1InMessage> entryAction() {
        return ctx -> System.out.println(
                "Entry " + ctx.getTarget().getId());
    }

    @Bean
    public Action<L1ControllerState, L1InMessage> exitAction() {
        return ctx -> System.out.println(
                "Exit " + ctx.getSource().getId() + " -> " + ctx.getTarget().getId());
    }
}