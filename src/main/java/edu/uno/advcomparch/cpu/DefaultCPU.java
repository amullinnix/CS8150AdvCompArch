package edu.uno.advcomparch.cpu;

import edu.uno.advcomparch.instruction.Instruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Queue;

public class DefaultCPU implements CentralProcessingUnit<String> {

    private Queue<String> queue;

    private StringBuilder responseStringBuilder = new StringBuilder();

    public DefaultCPU(Queue<String> queue) {
        this.queue = queue;
    }

    private final Logger outputLogger = LoggerFactory.getLogger("output");

    @Override
    public void read() {
        //TODO: stubbed out
    }

    @Override
    public void write() {
        //TODO: stubbed out
    }

    @Override
    public void data(byte[] data) {
        var dataString = "CPU Received Data: " + Arrays.toString(data);
        outputLogger.info(dataString);

        responseStringBuilder
                .append(dataString)
                .append(System.lineSeparator());
    }

    public void writeOutCPUReceivedData() {
        System.out.println(responseStringBuilder.toString());
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
