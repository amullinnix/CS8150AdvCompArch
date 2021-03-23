package edu.uno.advcomparch.controller;

import edu.uno.advcomparch.model.Data;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

// Requirements:
//      Unified, direct mapped, write-back, write-allocate same block size as L1 and 16KB size.
//      L2D is also dual-ported like L1.
//      L2 and L1 must support mutual inclusion policy, which means that if mutual inclusion is violated then you must do whatever is needed to restore it.
@Named
@lombok.Data
public class Level2Controller implements CacheController {

    private Queue<String> queue;

    //note: No sets in a direct mapped cache
    public static final int TAG_SIZE = 9;  //is the tag size the same?
    public static final int BLOCK_SIZE = 32;
    public static final int NUMBER_OF_SETS = 512;   // 16KB divided by 32 byte blocks = 512 Cache Blocks
    //offset is 5 bits + 9 bit tag = 14 bits?

    //not sure if this is correct
    List<CacheBlock> data;

    public Level2Controller(Queue<String> queue) {

        //initialize the cache
        data = new ArrayList<>();
        for(int i = 0; i < NUMBER_OF_SETS; i++) {
            data.add(new CacheBlock(TAG_SIZE, BLOCK_SIZE));
        }

        this.queue = queue;
    }

    @Override
    public Data<?> cpuRead() {
        throw new UnsupportedOperationException("cpuRead - Unsupported Operation");
    }

    @Override
    public void cpuWrite(Data<?> data) {
        throw new UnsupportedOperationException("cpuWrite - Unsupported Operation");
    }

    public boolean isDataPresentInCache(Address address) {

        //get the tag
        int tag = address.getTagDecimal();

        CacheBlock block = data.get(tag);

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
        CacheBlock blockFromCache = data.get(tag);

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
        CacheBlock block = data.get(address.getTagDecimal());

        if(block.isEmpty()) {
            System.out.println("block is empty, just letting you know");
        }

        return block;

    }

    public void printData() {
        System.out.println("Printing data of length: " + data.size());

        for(int i = 0; i < data.size(); i++) {
            printSingleCacheBlock(i);
        }
    }

    public void printSingleCacheBlock(int blockToPrint) {

        CacheBlock block = data.get(blockToPrint);

        System.out.print("Block #" + blockToPrint + ": ");
        System.out.print("t: " + Arrays.toString(block.getTag()));
        System.out.print("b: " + Arrays.toString(block.getBlock()));
        System.out.println();

    }

    @Override
    public void enqueueMessage(String message) {
        getQueue().add(message);
    }
}
