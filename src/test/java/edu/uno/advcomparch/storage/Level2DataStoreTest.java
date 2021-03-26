package edu.uno.advcomparch.storage;

import edu.uno.advcomparch.controller.Address;
import edu.uno.advcomparch.controller.CacheBlock;
import edu.uno.advcomparch.controller.ControllerState;
import edu.uno.advcomparch.statemachine.L1InMessage;
import org.junit.Before;
import org.junit.Test;
import org.springframework.messaging.Message;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

public class Level2DataStoreTest {


    private Level2DataStore dataStore;

    private CacheBlock blockToWrite;

    @Before
    public void setup() {
        Queue<Message<L1InMessage>> queue = new LinkedBlockingQueue<>();

        dataStore = new Level2DataStore();
        blockToWrite = new CacheBlock(9, 32);

        byte[] bytes = new byte[]{1,2,3,4};
        System.arraycopy(bytes, 0, blockToWrite.getBlock(), 0, bytes.length);
    }

    @Test
    public void dataNotInEmptyCache() {
        Address address = new Address("101010101", "", "00010");

        var isHit = dataStore.isDataPresentInCache(address);

        assertNotSame(isHit, ControllerState.HIT);

        dataStore.printData();
    }

    @Test
    public void writeDataToCache() {
        Address address = new Address("101010101", "", "00010");

        dataStore.writeDataToCache(address, blockToWrite);

        dataStore.printSingleCacheBlock(341);

        var isHit = dataStore.isDataPresentInCache(address);

        assertSame(isHit, ControllerState.HIT);
    }

    @Test
    public void getBlockFromCache() {
        Address address = new Address("101010101", "", "00010");

        dataStore.writeDataToCache(address, blockToWrite);

        CacheBlock blockFromCache = dataStore.getBlockAtAddress(address);

        assertEquals(Arrays.toString(blockToWrite.getBlock()), Arrays.toString(blockFromCache.getBlock()));
    }

    @Test
    public void getEmptyBlockFromCache() {
        Address address = new Address("101010101", "", "00010");

        CacheBlock block = dataStore.getBlockAtAddress(address);

        assertTrue(block.isEmpty());
    }

    /**
     * I'm still honestly not sure how this is supposed to look. I'm guessing this has to be
     * the dirty bit stuff, but we'll get to that.
     */
    @Test
    public void blockNotEmpty() {
        Address address = new Address("000000101", "", "00010");

        dataStore.writeDataToCache(address, blockToWrite);

        dataStore.printSingleCacheBlock(5);

        //new block, same address
        byte[] bytes = new byte[]{5,6,7,8};
        System.arraycopy(bytes, 0, blockToWrite.getBlock(), 0, bytes.length);
        dataStore.writeDataToCache(address, blockToWrite);

        dataStore.printSingleCacheBlock(5);

        CacheBlock blockFromCache = dataStore.getBlockAtAddress(address);
        assertEquals(Arrays.toString(blockToWrite.getBlock()), Arrays.toString(blockFromCache.getBlock()));

        //we need some assert for the eviction buffer thing a ma jig

        //Here's another point - should writes always succeed? Said differently, is it the responsibility of the
        //write method to do the eviction? Maybe not so much.
    }


    /**
     * Writing this test to expose a flaw where we sometimes forget about java references ;)
     */
    @Test
    public void bewareOfJavaReferences() {
        Address address = new Address("101010101", "", "00010");

        dataStore.writeDataToCache(address, blockToWrite);

        dataStore.printSingleCacheBlock(341);

        //new block, same address
        byte[] bytes = new byte[]{5,6,7,8};
        System.arraycopy(bytes, 0, blockToWrite.getBlock(), 0, bytes.length);

        //do NOTHING here! Cache block should not change!  (normally, we'd write the block)

        dataStore.printSingleCacheBlock(341);

        CacheBlock blockFromCache = dataStore.getBlockAtAddress(address);
        byte[] subSetOfBlock = Arrays.copyOfRange(blockFromCache.getBlock(), 0, 4);
        assertEquals(Arrays.toString(new byte[] {1,2,3,4}), Arrays.toString(subSetOfBlock));
    }
}