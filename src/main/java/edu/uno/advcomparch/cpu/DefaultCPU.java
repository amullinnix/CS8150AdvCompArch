package edu.uno.advcomparch.cpu;

import edu.uno.advcomparch.instruction.Instruction;

import java.util.List;
import java.util.Queue;

public class DefaultCPU implements CentralProcessingUnit<String> {

    private Queue<String> queue;

    public DefaultCPU(Queue<String> queue) {
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

    @Override
    public void data(String data) {
        System.out.println("Received Data: " + data);
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
