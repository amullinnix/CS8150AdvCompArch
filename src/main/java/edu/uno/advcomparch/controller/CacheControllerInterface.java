package edu.uno.advcomparch.controller;

import edu.uno.advcomparch.model.Data;
import edu.uno.advcomparch.statemachine.ControllerMessage;
import org.springframework.messaging.Message;

public interface CacheControllerInterface {

    // service a read request from the CPU.
    Data<?> cpuRead();

    // service a write request from the CPU.
    void cpuWrite(Data<?> data);

    // place a controller message on the queue to be processed.
    void enqueueMessage(Message<ControllerMessage> message);

}
