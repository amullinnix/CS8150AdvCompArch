package edu.uno.advcomparch.cpu;

import edu.uno.advcomparch.instruction.Message;

import java.util.Queue;

public class DefaultCPU  implements  CentralProcessingUnit{

    private Queue<Message> queue;

    public DefaultCPU(Queue<Message> queue) {
        this.queue = queue;
    }

    @Override
    public void read() {
        //TODO: stubbed out
    }

    @Override
    public void write() {
        //TODO: stubbed out
    }
}
