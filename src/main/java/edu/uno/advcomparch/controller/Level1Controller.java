package edu.uno.advcomparch.controller;

import edu.uno.advcomparch.instruction.Message;
import edu.uno.advcomparch.model.Data;
import edu.uno.advcomparch.storage.Level1DataStore;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
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

    private Queue<Message> queue;

    private Level1DataStore dataStore;

    //this class still needs states, the controller has a state ;)

    public Level1Controller(Queue<Message> queue) {

        this.messageList = new ArrayList<>();
        this.queue = queue;
    }

    //TODO: Revamp this method for the "new" controller logic
    public void processInstruction() {
        //I expect this to "put" instructions or messages on other queues, for now, using a message list

        //        //Get the first message from our queue
//        Message message = this.queue.poll();
//
//        if(message == null) {
//            return; //fail fast
//        }
//
//        Instruction instruction = message.getInstruction();
//
//        //Check level 1 data for a "hit"
//        //TODO: Needs to be an actual address
//        String address = instruction.getAddress();
//
//        //If we found hit, return to "source"
//        if( isDataPresentInCache(new Address()) ) {
//            messageList.add("Address " + address + " found, returning " + data);
//        }
//        //else request from next level down

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
    public void enqueueMessage(Message message) {
        getQueue().add(message);
    }
}
