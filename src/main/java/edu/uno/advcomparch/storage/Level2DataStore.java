package edu.uno.advcomparch.storage;

import edu.uno.advcomparch.controller.Address;
import edu.uno.advcomparch.controller.CacheBlock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Level2DataStore {

    //Same as Level 1 Data Store, this will take requests from Level 2 controller and hand back
    // (booleans, cache blocks, etc)

    //note: No sets in a direct mapped cache
    public static final int TAG_SIZE = 9;  //is the tag size the same?
    public static final int BLOCK_SIZE = 32;
    public static final int NUMBER_OF_SETS = 512;   // 16KB divided by 32 byte blocks = 512 Cache Blocks
    //offset is 5 bits + 9 bit tag = 14 bits?

    List<CacheBlock> cache;

    public Level2DataStore() {
        //initialize the cache
        cache = new ArrayList<>();
        for(int i = 0; i < NUMBER_OF_SETS; i++) {
            cache.add(new CacheBlock(TAG_SIZE, BLOCK_SIZE));
        }
    }

    public boolean isDataPresentInCache(Address address) {

        //get the tag
        int tag = address.getTagDecimal();

        CacheBlock block = cache.get(tag);

        if(address.getTag().equals(new String(block.getTag()))) {
            return true;
        }

        return false;
    }

    public void writeDataToCache(Address address, CacheBlock blockToWrite) {
        //get the tag
        int tag = address.getTagDecimal();
        System.out.println("Tag is: " + tag);

        //naive approach, check if the block is empty
        CacheBlock blockFromCache = cache.get(tag);

        if(blockFromCache.isEmpty()) {
            System.out.println("block is empty!");
            blockFromCache.setTag(address.getTag().getBytes());

            byte[] cacheBlock = blockFromCache.getBlock();
            byte[] bytesToWrite = blockToWrite.getBlock();
            System.arraycopy(bytesToWrite, 0, cacheBlock, 0, bytesToWrite.length);
        } else {
            System.out.println("block is NOT empty!");
            //is this where we need to do an eviction?
        }
    }


    public CacheBlock getBlockAtAddress(Address address) {
        CacheBlock block = cache.get(address.getTagDecimal());

        if(block.isEmpty()) {
            System.out.println("block is empty, just letting you know");
        }

        return block;
    }

    public void printData() {
        System.out.println("Printing data of length: " + cache.size());

        for(int i = 0; i < cache.size(); i++) {
            printSingleCacheBlock(i);
        }
    }

    public void printSingleCacheBlock(int blockToPrint) {

        CacheBlock block = cache.get(blockToPrint);

        System.out.print("Block #" + blockToPrint + ": ");
        System.out.print("t: " + Arrays.toString(block.getTag()));
        System.out.print("b: " + Arrays.toString(block.getBlock()));
        System.out.println();

    }

}