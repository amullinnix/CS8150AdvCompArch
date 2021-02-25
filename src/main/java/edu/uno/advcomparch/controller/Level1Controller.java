package edu.uno.advcomparch.controller;

import edu.uno.advcomparch.instruction.Instruction;
import edu.uno.advcomparch.instruction.Message;
import lombok.Data;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

//TODO: Do we want this class to implement the Cache Controller Interface?
@Data
public class Level1Controller {

    private Map<Address, String> level1Data;

    private List<String> messageList;

    private Queue<Message> queue;

    public Level1Controller(Queue<Message> queue) {
        this.level1Data = new HashMap<>();
        this.messageList = new ArrayList<>();
        this.queue = queue;
    }

    //I think this might be the interface method we are looking for
    public void processInstruction() {
        //I expect this to "put" instructions or messages on other queues, for now, using a message list

        //Do we need to calculate the address components?

        //Get the first message from our queue
        Message message = this.queue.poll();

        if(message == null) {
            return; //fail fast
        }
        
        Instruction instruction = message.getInstruction();

        //Check level 1 data for a "hit"
        String address = instruction.getAddress();

        Address addressToCheck = new Address();
        addressToCheck.setTag(address);

        String data = level1Data.get(addressToCheck);
        //If we found hit, return to "source"
        if( data != null ) {
            messageList.add("Address " + address + " found, returning " + data);
        }
        //else request from next level down

    }
}
