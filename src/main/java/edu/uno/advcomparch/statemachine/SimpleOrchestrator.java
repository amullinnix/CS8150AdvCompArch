package edu.uno.advcomparch.statemachine;

import edu.uno.advcomparch.controller.Level1Controller;
import edu.uno.advcomparch.cpu.DefaultCPU;
import edu.uno.advcomparch.repository.DataRepository;
import edu.uno.advcomparch.storage.DynamicRandomAccessMemory;
import edu.uno.advcomparch.utility.InstructionFileReaderUtility;

import javax.inject.Inject;

public class SimpleOrchestrator {

    private final DefaultCPU cpu;
    private final Level1Controller level1Controller;
    private final DataRepository level1DataRepository;
    private final DynamicRandomAccessMemory memory;


    @Inject
    public SimpleOrchestrator(DefaultCPU cpu,
                              Level1Controller level1Controller,
                              DataRepository level1DataRepository,
                              DynamicRandomAccessMemory memory) {
        this.cpu = cpu;
        this.level1Controller = level1Controller;
        this.level1DataRepository = level1DataRepository;
        this.memory = memory;
    }

    public void runSimulation(String filename) throws Exception {
        // TODO - Some instantiation may be required
        var instructions = InstructionFileReaderUtility.readInstruction(filename);

        cpu.processInstructionSet(instructions);
    }

}
