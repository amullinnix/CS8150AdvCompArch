package edu.uno.advcomparch.utility;

import edu.uno.advcomparch.instruction.Instruction;
import edu.uno.advcomparch.instruction.Message;
import edu.uno.advcomparch.model.Controller;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Queue;

@Data
@AllArgsConstructor
public class Router {

    //TODO: I already feel like this is ugly, but what of it?
    private Queue<Message> level1Queue;
    private Queue<Message> cpuQueue;

    public void route(Message message) {
        Controller destination = message.getDestination();

        switch (destination){
            case LEVEL_1_CACHE:
                level1Queue.offer(message);
            case CPU:
                cpuQueue.offer(message);
        }

    }
}
