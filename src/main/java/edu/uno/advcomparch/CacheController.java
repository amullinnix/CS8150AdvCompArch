package edu.uno.advcomparch;

import org.springframework.stereotype.Controller;

import java.util.LinkedList;
import java.util.Queue;

@Controller
public class CacheController {

    Queue<Instruction> queue;

    {
        queue = new LinkedList<>();
    }

    public boolean stubMethod() {
        return true;
    }


    public Queue<Instruction> getQueue() {

        return this.queue;
    }

    public void processMessage() {
        //Get instruction from queue
        Instruction instruction = this.queue.poll();

        //ostensibly do something with it ;)
    }
}
