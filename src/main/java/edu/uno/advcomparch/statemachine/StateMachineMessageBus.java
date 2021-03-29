package edu.uno.advcomparch.statemachine;

import lombok.Data;
import org.springframework.messaging.Message;

import javax.inject.Named;
import java.util.LinkedList;
import java.util.Queue;

@Data
@Named
public class StateMachineMessageBus {

    private Queue<Message<ControllerMessage>> l1MessageQueue;

    private Queue<Message<ControllerMessage>> l2MessageQueue;

    public Queue<Message<ControllerMessage>> getL1MessageQueue() {
        if (l1MessageQueue == null) {
            l1MessageQueue = new LinkedList<>();
        }

        return l1MessageQueue;
    }

    public void enqueueL1Message(Message<ControllerMessage> message) {
        getL1MessageQueue().add(message);
    }

    public Queue<Message<ControllerMessage>> getL2MessageQueue() {
        if (l2MessageQueue == null) {
            l2MessageQueue = new LinkedList<>();
        }

        return l2MessageQueue;
    }

    public void enqueueL2Message(Message<ControllerMessage> message) {
        getL2MessageQueue().add(message);
    }
}
