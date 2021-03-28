package edu.uno.advcomparch;

import edu.uno.advcomparch.statemachine.L1ControllerState;
import edu.uno.advcomparch.statemachine.L1InMessage;
import edu.uno.advcomparch.statemachine.StateMachineMessageBus;
import edu.uno.advcomparch.utility.MessageReaderUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Controller;

import java.io.FileNotFoundException;

@Controller
public class CacheController {

    @Autowired
    StateMachine<L1ControllerState, L1InMessage> l1StateMachine;

    @Autowired
    StateMachine<L1ControllerState, L1InMessage> l2StateMachine;

    @Autowired
    StateMachineMessageBus messageBus;

    public void runCache(String file) throws FileNotFoundException {
        var messages = MessageReaderUtility.readMessages(file);

        // Enqueue L1 Messages
        messages.forEach(message -> {
            messageBus.enqueueL1Message(message);
        });

        l1StateMachine.start();
        l2StateMachine.start();
    }
}
