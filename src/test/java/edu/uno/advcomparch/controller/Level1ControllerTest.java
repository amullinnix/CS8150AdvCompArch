package edu.uno.advcomparch.controller;

import edu.uno.advcomparch.instruction.Instruction;
import edu.uno.advcomparch.instruction.Message;
import edu.uno.advcomparch.statemachine.L1InMessage;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
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
        instruction.setType(L1InMessage.CPUREAD);
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
        Address address = new Address("101010", "101010", "10101");

        boolean isHit = controller.isDataPresentInCache(address);

        assertFalse(isHit);

        controller.printData();
    }

    @Test
    public void writeDataToCache() {
        Address address = new Address("101010", "101010", "10101");

        byte b = 2;
        controller.writeDataToCache(address, b);

        controller.printData();

        boolean isHit = controller.isDataPresentInCache(address);

        assertTrue(isHit);
    }

    @Test
    public void storeTwoAddresses() {
        Address address1 = new Address("101010", "101010", "10101");
        Address address2 = new Address("010101", "101010", "10101");

        byte b1 = 2;
        byte b2 = 4;

        controller.writeDataToCache(address1, b1);
        controller.printSingleSet(42);
        System.out.println("\n\nsecond write");
        controller.writeDataToCache(address2, b2);
        controller.printSingleSet(42);

        //both should be present, in the same set
        assertEquals(true, controller.isDataPresentInCache(address1));
        assertEquals(true, controller.isDataPresentInCache(address2));
    }

    @Test
    public void getDataFromCache() {
        Address address = new Address("000111", "000100", "00101");
        byte b = 13;

        controller.writeDataToCache(address, b);
        byte fromCache = controller.getByteAtAddress(address);
        controller.printSingleSet(4);

        assertEquals(b, fromCache);
    }

    //This test needs to come after being able to write to multiple locations in same block
    @Test
    public void getMultipleBytes() {
        Address address = new Address("000111", "000100", "00100");
        byte b = 13;
        controller.writeDataToCache(address, b);

        address = new Address("000111", "000100", "00101");
        b = 14;
        controller.writeDataToCache(address, b);

        address = new Address("000111", "000100", "00110");
        b = 15;
        controller.writeDataToCache(address, b);
        controller.printSingleSet(4);

        //reset address offset ;)
        address.setOffset("00100");

        //CPURead A 3
        byte[] fromCache = controller.getDataAtAddress(address, 3);

        assertEquals(true, Arrays.equals(new byte[] {13, 14, 15}, fromCache));
    }

    @Test
    public void writeMultipleBytesAtOnce() {
        Address address = new Address("000111", "000100", "00100");
        byte[] bytesToWrite = new byte[] {13, 14, 15};
        controller.writeDataToCache(address, bytesToWrite);

        controller.printSingleSet(4);

        //reset address offset ;)
        address.setOffset("00100");

        byte[] fromCache = controller.getDataAtAddress(address, 3);

        //trying out a different assert for fun
        assertEquals(Arrays.toString(new byte[] {13, 14, 15}), Arrays.toString(fromCache));
    }

    @Test
    public void canWriteToSetWithEmptyBlocks() {
        Address address = new Address("000111", "000100", "00100");

        assertEquals(true, controller.canWriteToCache(address));
    }

    @Test
    public void canWriteToFullSetWithMatchingTag() {
        Address address = new Address("000100", "000100", "00100");
        byte b = 1;
        controller.writeDataToCache(address, b);

        address.setTag("000101");  //same set, different tag
        controller.writeDataToCache(address, b);

        address.setTag("000110");  //same set, different tag
        controller.writeDataToCache(address, b);

        address.setTag("000111");  //same set, different tag
        controller.writeDataToCache(address, b);

        controller.printSingleSet(4);

        assertEquals(true, controller.canWriteToCache(address));
    }

    @Test
    public void cannotWriteToFullSet() {
        Address address = new Address("000100", "000100", "00100");
        byte b = 1;
        controller.writeDataToCache(address, b);

        address.setTag("000101");  //same set, different tag
        controller.writeDataToCache(address, b);

        address.setTag("000110");  //same set, different tag
        controller.writeDataToCache(address, b);

        address.setTag("000111");  //same set, different tag
        controller.writeDataToCache(address, b);

        controller.printSingleSet(4);

        address.setTag("001000");  //set should be full at this point, no room for new tag

        assertEquals(false, controller.canWriteToCache(address));
    }

    /**
     * Purpose of this test is basically to force us to implement getting a full cache block. In other words,
     * if a set is full, and we have a new tag, then we are going to have to evict a cache block. Let's start
     * by retrieving the one we want to evict. What we do with it can be implemented later.
     */
    @Test
    public void getFullSet() {
        Address address = new Address("000100", "000100", "00100");
        byte[] b = new byte[]{13, 14, 15};

        //no need to fill cache up, one write will suffice
        controller.writeDataToCache(address, b);

        CacheBlock block = controller.getCacheBlock(address);

        assertEquals("000100", block.getTagString());

        byte[] expected = new byte[]{0,0,0,0,13,14,15,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        assertEquals(Arrays.toString(expected), Arrays.toString(block.getBlock()));

    }
}