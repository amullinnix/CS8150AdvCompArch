package edu.uno.advcomparch.statemachine;

import edu.uno.advcomparch.controller.Level1Controller;
import edu.uno.advcomparch.cpu.DefaultCPU;
import edu.uno.advcomparch.repository.DataRepository;
import edu.uno.advcomparch.storage.DynamicRandomAccessMemory;
import edu.uno.advcomparch.utility.InstructionFileReaderUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;

import javax.inject.Inject;

public class StateMachineRunner {

    private final DefaultCPU cpu;
    private final Level1Controller level1Controller;
    private final DataRepository level1DataRepository;
    private final DynamicRandomAccessMemory memory;

    @Autowired
    private StateMachine<L1ControllerState, L1InMessage> stateMachine;

    @Inject
    public StateMachineRunner(DefaultCPU cpu,
                              Level1Controller level1Controller,
                              DataRepository level1DataRepository,
                              DynamicRandomAccessMemory memory) {
        this.cpu = cpu;
        this.level1Controller = level1Controller;
        this.level1DataRepository = level1DataRepository;
        this.memory = memory;
    }


    public void runStateMachine() throws Exception {
        var instructions = InstructionFileReaderUtility.readInstruction("test.txt");
        cpu.processInstructionSet(instructions);

        stateMachine.start();

        // input events to the StateMachine
        instructions.forEach(instruction -> {
            stateMachine.getExtendedState().getVariables().put("message", instruction.toString());
            stateMachine.sendEvent(instruction.getType());
        });

        // Can use headers to fetch from extend state
//        Message<L1InMessage> message = MessageBuilder
//                .withPayload(L1InMessage.CPUREAD)
//                .setHeader("foo", "bar")
//                .build();
//         stateMachine.sendEvent(message);


    }
}
