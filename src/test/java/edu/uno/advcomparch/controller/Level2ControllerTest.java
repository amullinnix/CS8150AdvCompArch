package edu.uno.advcomparch.controller;

import edu.uno.advcomparch.instruction.Message;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

public class Level2ControllerTest {

    private Level2Controller controller;

    @Before
    public void setup() {
        Queue<Message> queue = new LinkedBlockingQueue<>();
        controller = new Level2Controller(queue);
    }

    @Test
    public void dataNotInEmptyCache() {
        Address address = new Address("101010101", "", "00010");

        boolean isHit = controller.isDataPresentInCache(address);

        assertFalse(isHit);

        controller.printData();
    }

    @Test
    public void writeDataToCache() {
        Address address = new Address("101010101", "", "00010");
        Byte b = 13;

        controller.writeDataToCache(address, b);

        controller.printSingleCacheBlock(341);

        boolean isHit = controller.isDataPresentInCache(address);

        assertTrue(isHit);
    }

    @Test
    public void getByteFromCache() {
        Address address = new Address("101010101", "", "00010");
        Byte b = 13;

        controller.writeDataToCache(address, b);

        Byte fromCache = controller.getByteAtAddress(address);

        assertEquals(b, fromCache);
    }

    @Test
    public void getByteFromEmptyBlock() {
        Address address = new Address("101010101", "", "00010");

        Byte fromCache = controller.getByteAtAddress(address);

        assertEquals(null, fromCache);
    }

    /**
     * I'm still honestly not sure how this is supposed to look. I'm guessing this has to be
     * the dirty bit stuff, but we'll get to that.
     */
    @Test
    public void blockAndByteNotEmpty() {
        Address address = new Address("101010101", "", "00010");
        Byte b = 13;

        controller.writeDataToCache(address, b);

        controller.printSingleCacheBlock(341);

        //new byte, same address
        b = 21;
        controller.writeDataToCache(address, b);

        controller.printSingleCacheBlock(341);

        assertEquals(b, controller.getByteAtAddress(address));

        //we need some assert for the eviction buffer thing a ma jig
    }

    @Test
    public void writeMultipleBytes() {
        Address address = new Address("101010101", "", "00100");
        byte[] bytes = new byte[] {13, 14, 15};

        controller.writeDataToCache(address, bytes);

        controller.printSingleCacheBlock(341);

        //reset the offset
        address.setOffset("00100");

        byte[] fromCache = controller.getDataAtAddress(address, 3);

        assertEquals(Arrays.toString(bytes), Arrays.toString(fromCache));
    }
}