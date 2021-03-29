package edu.uno.advcomparch.controller;

import edu.uno.advcomparch.model.Data;
import edu.uno.advcomparch.statemachine.ControllerMessage;
import edu.uno.advcomparch.storage.Level2DataStore;
import org.springframework.messaging.Message;

import javax.inject.Named;
import java.util.Queue;

// Requirements:
//      Unified, direct mapped, write-back, write-allocate same block size as L1 and 16KB size.
//      L2D is also dual-ported like L1.
//      L2 and L1 must support mutual inclusion policy, which means that if mutual inclusion is violated then you must do whatever is needed to restore it.
@Named
@lombok.Data
public class Level2Controller implements CacheControllerInterface {

    private Queue<Message<ControllerMessage>> queue;

    private Level2DataStore dataStore;

    public Level2Controller(Queue<Message<ControllerMessage>> queue) {
        this.queue = queue;
    }

    @Override
    public Data<?> cpuRead() {
        throw new UnsupportedOperationException("cpuRead - Unsupported Operation");
    }

    @Override
    public void cpuWrite(Data<?> data) {
        throw new UnsupportedOperationException("cpuWrite - Unsupported Operation");
    }


    @Override
    public void enqueueMessage(Message<ControllerMessage> message) {
        getQueue().add(message);
    }
}
