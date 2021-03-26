package edu.uno.advcomparch.storage;

import edu.uno.advcomparch.controller.Address;
import edu.uno.advcomparch.controller.CacheBlock;
import edu.uno.advcomparch.controller.CacheSet;
import edu.uno.advcomparch.controller.ControllerState;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class Level1DataStoreTest {

    private Level1DataStore dataStore;

    @Before
    public void setup() {
        dataStore = new Level1DataStore();
    }

    @Test
    public void useByteArray() {

        //simulate a cache miss
        Address address = new Address("101010", "101010", "10101");

        var isHit = dataStore.isDataPresentInCache(address);

        assertNotSame(isHit, ControllerState.HIT);

        dataStore.printData();
    }

    @Test
    public void writeDataToCache() {
        Address address = new Address("101010", "101010", "10101");

        byte b = 2;
        dataStore.writeDataToCache(address, b);

        dataStore.printData();

        var isHit = dataStore.isDataPresentInCache(address);

        assertSame(isHit, ControllerState.HIT);
    }

    @Test
    public void storeTwoAddresses() {
        Address address1 = new Address("101010", "101010", "10101");
        Address address2 = new Address("010101", "101010", "10101");

        byte b1 = 2;
        byte b2 = 4;

        dataStore.writeDataToCache(address1, b1);
        dataStore.printSingleSet(42);
        System.out.println("\n\nsecond write");
        dataStore.writeDataToCache(address2, b2);
        dataStore.printSingleSet(42);

        //both should be present, in the same set
        assertEquals(true, dataStore.isDataPresentInCache(address1));
        assertEquals(true, dataStore.isDataPresentInCache(address2));
    }

    @Test
    public void getDataFromCache() {
        Address address = new Address("000111", "000100", "00101");
        byte b = 13;

        dataStore.writeDataToCache(address, b);
        byte fromCache = dataStore.getByteAtAddress(address);
        dataStore.printSingleSet(4);

        assertEquals(b, fromCache);
    }

    //This test needs to come after being able to write to multiple locations in same block
    @Test
    public void getMultipleBytes() {
        Address address = new Address("000111", "000100", "00100");
        byte b = 13;
        dataStore.writeDataToCache(address, b);

        address = new Address("000111", "000100", "00101");
        b = 14;
        dataStore.writeDataToCache(address, b);

        address = new Address("000111", "000100", "00110");
        b = 15;
        dataStore.writeDataToCache(address, b);
        dataStore.printSingleSet(4);

        //reset address offset ;)
        address.setOffset("00100");

        //CPURead A 3
        byte[] fromCache = dataStore.getDataAtAddress(address, 3);

        assertEquals(true, Arrays.equals(new byte[] {13, 14, 15}, fromCache));
    }

    @Test
    public void writeMultipleBytesAtOnce() {
        Address address = new Address("000111", "000100", "00100");
        byte[] bytesToWrite = new byte[] {13, 14, 15};
        dataStore.writeDataToCache(address, bytesToWrite);

        dataStore.printSingleSet(4);

        //reset address offset ;)
        address.setOffset("00100");

        byte[] fromCache = dataStore.getDataAtAddress(address, 3);

        //trying out a different assert for fun
        assertEquals(Arrays.toString(new byte[] {13, 14, 15}), Arrays.toString(fromCache));
    }

    @Test
    public void canWriteToSetWithEmptyBlocks() {
        Address address = new Address("000111", "000100", "00100");

        //TODO: fix this test
        assertEquals(ControllerState.MISSC, dataStore.canWriteToCache(address));
    }

    @Test
    public void canWriteToFullSetWithMatchingTag() {
        Address address = new Address("000100", "000100", "00100");
        byte b = 1;
        dataStore.writeDataToCache(address, b);

        address.setTag("000101");  //same set, different tag
        dataStore.writeDataToCache(address, b);

        address.setTag("000110");  //same set, different tag
        dataStore.writeDataToCache(address, b);

        address.setTag("000111");  //same set, different tag
        dataStore.writeDataToCache(address, b);

        dataStore.printSingleSet(4);

        //TODO: fix this test
        assertEquals(ControllerState.MISSI, dataStore.canWriteToCache(address));
    }

    @Test
    public void cannotWriteToFullSet() {
        Address address = new Address("000100", "000100", "00100");
        byte b = 1;
        dataStore.writeDataToCache(address, b);

        address.setTag("000101");  //same set, different tag
        dataStore.writeDataToCache(address, b);

        address.setTag("000110");  //same set, different tag
        dataStore.writeDataToCache(address, b);

        address.setTag("000111");  //same set, different tag
        dataStore.writeDataToCache(address, b);

        dataStore.printSingleSet(4);

        address.setTag("001000");  //set should be full at this point, no room for new tag

        //TODO: fix this test
        assertEquals(ControllerState.MISSC, dataStore.canWriteToCache(address));
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
        dataStore.writeDataToCache(address, b);

        CacheBlock block = dataStore.getCacheBlock(address);

        assertEquals("000100", block.getTagString());

        byte[] expected = new byte[]{0,0,0,0,13,14,15,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        assertEquals(Arrays.toString(expected), Arrays.toString(block.getBlock()));

    }

    @Test
    public void dirtyBitIsSetOnWrite() {
        Address address = new Address("000100", "000100", "00100");
        byte b = 1;
        dataStore.writeDataToCache(address, b);

        CacheBlock cacheBlock = dataStore.getCacheBlock(address);

        assertTrue(cacheBlock.isDirty());
    }

    @Test
    public void dirtyBitSetOnReWrite() {
        Address address = new Address("000100", "000100", "00100");
        byte b = 1;
        dataStore.writeDataToCache(address, b);

        address.setOffset("00011");
        b = 2;
        dataStore.writeDataToCache(address, b);

        dataStore.printSingleSet(4);

        CacheBlock cacheBlock = dataStore.getCacheBlock(address);

        assertTrue(cacheBlock.isDirty());
    }

    @Test
    public void getCacheSet() {
        Address address = new Address("000100", "000100", "00100");
        byte b = 1;
        dataStore.writeDataToCache(address, b);

        CacheSet set = dataStore.getCacheSet(address);

        assertTrue(set.containsTag(address));
    }

    @Test
    public void whatHappensWhenWeWriteAfterRemovingBlock() {
        Address address = new Address("000100", "000100", "00100");
        byte b = 1;
        dataStore.writeDataToCache(address, b);

        CacheSet cacheSet = dataStore.getCacheSet(address);
        dataStore.printSingleSet(4);
        cacheSet.removeBlock(address);
        dataStore.printSingleSet(4);

        address.setOffset("00010");
        b = 3;
        dataStore.writeDataToCache(address, b);
        dataStore.printSingleSet(4);

    }

    @Test
    public void validBitTrueAfterWriting() {
        Address address = new Address("000100", "000100", "00100");
        byte b = 1;

        dataStore.writeDataToCache(address, b);

        CacheBlock cacheBlock = dataStore.getCacheBlock(address);

        assertTrue(cacheBlock.isValid());
    }

    @Test
    public void determineMissType() {
        Address address = new Address("000100", "000100", "00100");
        byte b = 1;

        dataStore.writeDataToCache(address, b);
    }
}