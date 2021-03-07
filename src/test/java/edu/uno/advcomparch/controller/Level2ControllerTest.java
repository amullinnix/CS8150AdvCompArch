package edu.uno.advcomparch.controller;

import edu.uno.advcomparch.instruction.Message;
import org.junit.Before;
import org.junit.Test;

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
        Address address = new Address();

        boolean isHit = controller.isDataPresentInCache(address);

        assertFalse(isHit);

        controller.printData();
    }

    @Test
    public void writeDataToCache() {
        Address address = new Address("101010101", "", "0010");
        Byte b = 13;

        controller.writeDataToCache(address, b);

        controller.printSingleCacheBlock(341);

        boolean isHit = controller.isDataPresentInCache(address);

        assertTrue(isHit);
    }
}