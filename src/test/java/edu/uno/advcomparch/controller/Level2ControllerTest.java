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

    private CacheBlock blockToWrite;

    @Before
    public void setup() {
        Queue<Message> queue = new LinkedBlockingQueue<>();

        controller = new Level2Controller(queue);
        blockToWrite = new CacheBlock(9, 32);

        byte[] bytes = new byte[]{1,2,3,4};
        System.arraycopy(bytes, 0, blockToWrite.getBlock(), 0, bytes.length);
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

        controller.writeDataToCache(address, blockToWrite);

        controller.printSingleCacheBlock(341);

        boolean isHit = controller.isDataPresentInCache(address);

        assertTrue(isHit);
    }

    @Test
    public void getBlockFromCache() {
        Address address = new Address("101010101", "", "00010");

        controller.writeDataToCache(address, blockToWrite);

        CacheBlock blockFromCache = controller.getBlockAtAddress(address);

        assertEquals(Arrays.toString(blockToWrite.getBlock()), Arrays.toString(blockFromCache.getBlock()));
    }

    @Test
    public void getEmptyBlockFromCache() {
        Address address = new Address("101010101", "", "00010");

        CacheBlock block = controller.getBlockAtAddress(address);

        assertTrue(block.isEmpty());
    }

    /**
     * I'm still honestly not sure how this is supposed to look. I'm guessing this has to be
     * the dirty bit stuff, but we'll get to that.
     */
    @Test
    public void blockNotEmpty() {
        Address address = new Address("000000101", "", "00010");

        controller.writeDataToCache(address, blockToWrite);

        controller.printSingleCacheBlock(341);

        //new block, same address
        byte[] bytes = new byte[]{5,6,7,8};
        System.arraycopy(bytes, 0, blockToWrite.getBlock(), 0, bytes.length);
        controller.writeDataToCache(address, blockToWrite);

        controller.printSingleCacheBlock(341);

        CacheBlock blockFromCache = controller.getBlockAtAddress(address);
        assertEquals(Arrays.toString(blockToWrite.getBlock()), Arrays.toString(blockFromCache.getBlock()));

        //we need some assert for the eviction buffer thing a ma jig
    }


    /**
     * Writing this test to expose a flaw where we sometimes forget about java references ;)
     */
    @Test
    public void bewareOfJavaReferences() {
        Address address = new Address("101010101", "", "00010");

        controller.writeDataToCache(address, blockToWrite);

        controller.printSingleCacheBlock(341);

        //new block, same address
        byte[] bytes = new byte[]{5,6,7,8};
        System.arraycopy(bytes, 0, blockToWrite.getBlock(), 0, bytes.length);

        //do NOTHING here! Cache block should not change!  (normally, we'd write the block)

        controller.printSingleCacheBlock(341);

        CacheBlock blockFromCache = controller.getBlockAtAddress(address);
        byte[] subSetOfBlock = Arrays.copyOfRange(blockFromCache.getBlock(), 0, 4);
        assertEquals(Arrays.toString(new byte[] {1,2,3,4}), Arrays.toString(subSetOfBlock));
    }

}