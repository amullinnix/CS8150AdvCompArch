package edu.uno.advcomparch;

import edu.uno.advcomparch.statemachine.ControllerMessage;
import edu.uno.advcomparch.statemachine.ControllerState;
import edu.uno.advcomparch.statemachine.StateMachineMessageBus;
import edu.uno.advcomparch.utility.MessageReaderUtility;
import org.springframework.statemachine.StateMachine;

import javax.inject.Inject;
import java.io.FileNotFoundException;

public class CacheController {

    private final StateMachine<ControllerState, ControllerMessage> l1StateMachine;
    private final StateMachine<ControllerState, ControllerMessage> l2StateMachine;
    private final StateMachine<ControllerState, ControllerMessage> dramStateMachine;

    private final StateMachineMessageBus messageBus;

    @Inject
    public CacheController(StateMachine<ControllerState, ControllerMessage> l1StateMachine,
                           StateMachine<ControllerState, ControllerMessage> l2StateMachine,
                           StateMachine<ControllerState, ControllerMessage> dramStateMachine,
                           StateMachineMessageBus messageBus) {

        this.l1StateMachine = l1StateMachine;
        this.l2StateMachine = l2StateMachine;
        this.dramStateMachine = dramStateMachine;

        this.messageBus = messageBus;
    }

    public void runCacheOnFile(String file) throws FileNotFoundException {
        var messages = MessageReaderUtility.readMessages(file);

        // Enqueue L1 Messages
        messages.forEach(messageBus::enqueueL1Message);

        l1StateMachine.start();
        l2StateMachine.start();

    }
}
