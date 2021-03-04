package edu.uno.advcomparch.controller;

import edu.uno.advcomparch.instruction.Instruction;
import edu.uno.advcomparch.instruction.InstructionType;
import edu.uno.advcomparch.instruction.Message;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.*;

public class Level1ControllerTest {

    private Level1Controller controller;

    @Before
    public void setup() {
        Queue<Message> queue = new LinkedBlockingQueue<>();
        controller = new Level1Controller(queue);

    }

    @Test
    public void processWithNoInstructions() {
        controller.processInstruction();
    }

    @Ignore
    @Test
    public void doFirstRead() {
        //TODO: This test needs to be rewritten for new logic
        Instruction instruction = new Instruction();
        instruction.setType(InstructionType.CPURead);
        instruction.setAddress("000000000000000001");

        Message message = new Message();
        message.setInstruction(instruction);
        controller.getQueue().offer(message);  //pre populate the queue w/ our first message
        //not worrying about src/dest atm

        //execute
        controller.processInstruction();

        //assert something here?
        assertTrue(controller.getMessageList().contains("Address A found, returning someData"));
    }

    @Test
    public void useByteArray() {

        //simulate a cache miss
        Address address = new Address();
        address.setTag("101010");
        address.setIndex("101010");
        address.setOffset("10101");

        boolean isHit = controller.isDataPresentInCache(address);

        assertFalse(isHit);

        controller.printData();
    }

    @Test
    public void writeDataToCache() {
        Address address = new Address();
        address.setTag("101010");
        address.setIndex("101010");
        address.setOffset("10101");

        byte b = 2;
        controller.writeDataToCache(address, b);

        controller.printData();

        boolean isHit = controller.isDataPresentInCache(address);

        assertTrue(isHit);
    }

    @Test
    public void storeTwoAddresses() {
        Address address1 = new Address();
        address1.setTag("101010");
        address1.setIndex("101010");
        address1.setOffset("10101");

        Address address2 = new Address();
        address2.setTag("010101");
        address2.setIndex("101010");
        address2.setOffset("10101");

        byte b1 = 2;
        byte b2 = 4;

        controller.writeDataToCache(address1, b1);
        controller.printData();
        System.out.println("\n\nsecond write");
        controller.writeDataToCache(address2, b2);
        controller.printData();

        //both should be present, in the same set
        assertEquals(true, controller.isDataPresentInCache(address1));
        assertEquals(true, controller.isDataPresentInCache(address2));
    }

}