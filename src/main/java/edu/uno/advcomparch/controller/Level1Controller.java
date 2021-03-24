package edu.uno.advcomparch.controller;

import edu.uno.advcomparch.model.Data;
import edu.uno.advcomparch.statemachine.L1InMessage;
import edu.uno.advcomparch.storage.Level1DataStore;
import org.springframework.messaging.Message;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

// Requirements:
//      Split, 4-way, write-back, write-allocate, 32 byte block size and size 8KB.
//      L1C must support valid, dirty bits.
//      Assume that L1D is dual-ported – one read and one write port.
@lombok.Data
@Named
public class Level1Controller implements CacheController {

    private List<String> messageList;

    private Queue<Message<L1InMessage>> queue;

    private Level1DataStore dataStore;

    //TODO: this class still needs states, the controller has a state ;)

    //TODO: This class still needs a write buffer and victim buffer. Do they belong here or in the dataStore?

    public Level1Controller(Queue<Message<L1InMessage>> queue) {

        this.messageList = new ArrayList<>();
        this.queue = queue;
    }

    //TODO: Revamp this method for the "new" controller logic
    public void processInstruction() {

        //Get the first message from our queue
        var message = this.queue.poll();

        if(message == null) {
            return; //fail fast
        }

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
    public void enqueueMessage(Message<L1InMessage> message) {
        getQueue().add(message);
    }
}
