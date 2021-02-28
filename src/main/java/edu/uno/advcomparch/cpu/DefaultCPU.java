package edu.uno.advcomparch.cpu;

import edu.uno.advcomparch.instruction.Instruction;
import edu.uno.advcomparch.instruction.Message;

import java.util.List;
import java.util.Queue;

public class DefaultCPU implements CentralProcessingUnit {

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

    /**
     * Centralized Processing Component - This might need to be extracted.
     *
     * @param instructions the list of parsed instructions from the input file.
     * @return the output text form (or we could return
     */
    public String processInstructionSet(List<Instruction> instructions) {
      return "";
    }
}
