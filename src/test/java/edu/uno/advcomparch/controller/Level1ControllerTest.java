package edu.uno.advcomparch.controller;

import edu.uno.advcomparch.instruction.Instruction;
import edu.uno.advcomparch.instruction.InstructionType;
import edu.uno.advcomparch.instruction.Message;
import org.junit.Before;
import org.junit.Test;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.assertTrue;

public class Level1ControllerTest {

    private Level1Controller controller;

    @Before
    public void setup() {
        Queue<Message> queue = new LinkedBlockingQueue<>();
        controller = new Level1Controller(queue);

        Address address = new Address();
        address.setTag("A");

        controller.getLevel1Data().put(address, "someData");
    }

    @Test
    public void processWithNoInstructions() {
        controller.processInstruction();
    }

    @Test
    public void doFirstRead() {

        Instruction instruction = new Instruction();
        instruction.setType(InstructionType.CPURead);
        instruction.setAddress("A");

        Message message = new Message();
        message.setInstruction(instruction);
        controller.getQueue().offer(message);  //pre populate the queue w/ our first message
        //not worrying about src/dest atm

        //execute
        controller.processInstruction();

        //assert something here?
        assertTrue(controller.getMessageList().contains("Address A found, returning someData"));
    }

}