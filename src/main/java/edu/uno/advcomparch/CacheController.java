package edu.uno.advcomparch;

import edu.uno.advcomparch.cpu.DefaultCPU;
import edu.uno.advcomparch.statemachine.ControllerMessage;
import edu.uno.advcomparch.statemachine.ControllerState;
import edu.uno.advcomparch.statemachine.StateMachineMessageBus;
import edu.uno.advcomparch.utility.MessageReaderUtility;
import org.springframework.statemachine.StateMachine;

import javax.inject.Inject;

public class CacheController {

    private final StateMachine<ControllerState, ControllerMessage> l1StateMachine;
    private final StateMachine<ControllerState, ControllerMessage> l2StateMachine;
    private final StateMachine<ControllerState, ControllerMessage> dramStateMachine;

    private final StateMachineMessageBus messageBus;
    private final DefaultCPU cpu;

    @Inject
    public CacheController(StateMachine<ControllerState, ControllerMessage> l1StateMachine,
                           StateMachine<ControllerState, ControllerMessage> l2StateMachine,
                           StateMachine<ControllerState, ControllerMessage> dramStateMachine,
                           StateMachineMessageBus messageBus,
                           DefaultCPU cpu) {

        this.l1StateMachine = l1StateMachine;
        this.l2StateMachine = l2StateMachine;
        this.dramStateMachine = dramStateMachine;

        this.messageBus = messageBus;
        this.cpu = cpu;
    }

    public void runCacheOnFile(String file) throws Exception {
        var messages = MessageReaderUtility.readMessages(file);

        // Enqueue L1 Messages
        messages.forEach(messageBus::enqueueCpuMessage);

        l1StateMachine.start();
        l2StateMachine.start();

        // Spin in oblivion waiting on state machines
        // probably better to utilize sychronized variable to wait on thread.
        // while(!l1StateMachine.isComplete()) {}

        // Dump what all was received by the CPU;
        cpu.writeOutCPUReceivedData();
    }
}
