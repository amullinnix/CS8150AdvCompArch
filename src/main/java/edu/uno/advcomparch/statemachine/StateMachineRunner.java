package edu.uno.advcomparch.statemachine;

import edu.uno.advcomparch.controller.Level1Controller;
import edu.uno.advcomparch.cpu.DefaultCPU;
import edu.uno.advcomparch.repository.DataRepository;
import edu.uno.advcomparch.storage.DynamicRandomAccessMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;

import javax.inject.Inject;

public class StateMachineRunner {

    private final DefaultCPU cpu;
    private final Level1Controller level1Controller;
    private final DataRepository level1DataRepository;
    private final DynamicRandomAccessMemory memory;

    @Autowired
    private StateMachine<ControllerState, ControllerMessage> stateMachine;

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
//        var instructions = MessageReaderUtility.readMessages("test.txt");
//        stateMachine.start();
//
//        // input events to the StateMachine
//        instructions.forEach(instruction -> {
//            // TODO - determine if we need to store the instruction in the extended state, and candence of messages
//            stateMachine.getExtendedState().getVariables().put("instruction", instruction);
////            stateMachine.sendEvent(MessageBuilder
////                    .withPayload(instruction.getType())
////                    .setHeader("address", instruction.getAddress())
////                    .setHeader("source", instruction.getSource())
////                    .build());
//        });
    }
}
