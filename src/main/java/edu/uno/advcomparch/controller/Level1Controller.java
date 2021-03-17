package edu.uno.advcomparch.controller;

import edu.uno.advcomparch.instruction.Instruction;
import edu.uno.advcomparch.instruction.Message;
import edu.uno.advcomparch.model.Data;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

// Requirements:
//      Split, 4-way, write-back, write-allocate, 32 byte block size and size 8KB.
//      L1C must support valid, dirty bits.
//      Assume that L1D is dual-ported – one read and one write port.
@lombok.Data
@Named
public class Level1Controller implements CacheController {

    //TODO: Consider calculating this and extracting as well
    public static final int NUMBER_OF_SETS = 64;
    public static final int NUMBER_OF_BLOCKS = 4;
    public static final int TAG_SIZE = 6;
    public static final int BLOCK_SIZE = 32;

    private List<CacheSet> data;  //maybe call this, I dunno, cache, instead? ;)

    private List<String> messageList;

    private Queue<Message> queue;

    public Level1Controller(Queue<Message> queue) {

        //Initialize the cache!
        data = new ArrayList<>();
        for(int i = 0; i < NUMBER_OF_SETS; i++) {
            CacheSet set = new CacheSet();
            for(int j = 0; j < NUMBER_OF_BLOCKS; j++) {
                CacheBlock block = new CacheBlock(TAG_SIZE, BLOCK_SIZE);
                set.add(block);
            }
            data.add(set);
        }


        this.messageList = new ArrayList<>();
        this.queue = queue;
    }

    //I think this might be the interface method we are looking for
    //TODO: Revamp this method for the "new" controller logic
    public void processInstruction() {
        //I expect this to "put" instructions or messages on other queues, for now, using a message list

        //Get the first message from our queue
        Message message = this.queue.poll();

        if(message == null) {
            return; //fail fast
        }
        
        Instruction instruction = message.getInstruction();

        //Check level 1 data for a "hit"
        //TODO: Needs to be an actual address
        String address = instruction.getAddress();

        //If we found hit, return to "source"
        if( isDataPresentInCache(new Address()) ) {
            messageList.add("Address " + address + " found, returning " + data);
        }
        //else request from next level down

    }

    //The only thing this really cares about is, if the tag is in the cache
    //It's quite possible this could be a "shared" method.
    public boolean isDataPresentInCache(Address address) {

        //fetch the set
        CacheSet set = data.get(address.getIndexDecimal());

        //compare the tag to our address' tag
        byte [] tagBytes = address.getTag().getBytes();
        System.out.println("Tag from Address: " + Arrays.toString(tagBytes));

        //less naive approach, check them all
        for(CacheBlock block : set.getBlocks()) {
            if(Arrays.equals(block.getTag(), tagBytes)) {
                return true;
            }
        }

        return false;
    }

    public void writeDataToCache(Address address, byte[] bytesToWrite) {

        for(int i = 0; i < bytesToWrite.length; i++) {
            this.writeDataToCache(address, bytesToWrite[i]);

            //must increment the offset by one to write the next byte in the correct location
            address.incrementOffset();
        }

    }

    //This method might be starting to violate single responsibility principle.
    public void writeDataToCache(Address address, byte b) {

        //fetch the set (with the 4 cache blocks)
        CacheSet set = data.get(address.getIndexDecimal());

        //less naive approach, find first empty (or matching) cache block
        CacheBlock blockToWrite = null;

        for(CacheBlock block : set.getBlocks()) {
            if(block.isEmpty()) {   //TODO: Java 8+ ify this.
                System.out.println("empty block found!");
                blockToWrite = block;
                break;  //I hate breaks
            } else {
                System.out.println("block not empty :(");
                if(Arrays.equals(block.getTag(), address.getTag().getBytes())) {
                    System.out.println("tag matches!");
                    blockToWrite = block;
                    break;  //still hate breaks
                }
            }
        }

        if(blockToWrite == null) {
            System.out.println("Set is full - oh noes!");
            return;  //should be an error, throw exception?
        }

        //write (or re-write) tag, and write the byte
        blockToWrite.setTag(address.getTag().getBytes());
        blockToWrite.getBlock()[address.getOffsetDecimal()] = b;
    }

    public byte getByteAtAddress(Address address) {

        //fetch the set
        CacheSet set = data.get(address.getIndexDecimal());

        //compare the tag to our address' tag
        byte [] tagBytes = address.getTag().getBytes();
        System.out.println("Tag from Address: " + Arrays.toString(tagBytes));

        //find the matching block
        for(CacheBlock block : set.getBlocks()) {
            if(Arrays.equals(block.getTag(), tagBytes)) {
                return block.getBlock()[address.getOffsetDecimal()];
            }
        }

        return 1;  //ugly
    }

    public byte[] getDataAtAddress(Address address, int bytesToRead) {

        byte[] bytes = new byte[]{};

        //fetch the set
        CacheSet set = data.get(address.getIndexDecimal());

        //find the matching block
        for(CacheBlock block : set.getBlocks()) {
            if(Arrays.equals(block.getTag(), address.getTag().getBytes())) {
                int decimalOffset = address.getOffsetDecimal();
                bytes = Arrays.copyOfRange(block.getBlock(), decimalOffset, decimalOffset + bytesToRead);
                break; //uggh!
            }
        }

        return bytes;
    }

    public boolean canWriteToCache(Address address) {
        CacheSet set = data.get(address.getIndexDecimal());

        for(CacheBlock block : set.getBlocks()) {
            if(block.isEmpty()) {
                return true;
            } else if(Arrays.equals(address.getTag().getBytes(), block.getTag())) {
                return true;  //TODO consider the dirty bit
            }
        }

        return false;
    }

    public CacheBlock getCacheBlock(Address address) {
        CacheSet cacheSet = data.get(address.getIndexDecimal());

        for(CacheBlock block : cacheSet.getBlocks()) {
            if(Arrays.equals(address.getTag().getBytes(), block.getTag())) {
                return block;
            }
        }

        return null;
    }

    public void printData() {

        System.out.println("Printing data of length: " + data.size());

        for(int i = 0; i < data.size(); i++) {
            printSingleSet(i);
            System.out.println();
        }
    }

    public void printSingleSet(int setToPrint) {

        CacheSet set = data.get(setToPrint);
        System.out.println("Set " + setToPrint + ": ");

        List<CacheBlock> blocks = set.getBlocks();
        for(CacheBlock block : blocks) {
            System.out.print("t: " + Arrays.toString(block.getTag()));
            System.out.print("b: " + Arrays.toString(block.getBlock()));
            System.out.println();
        }
    }

    @Override
    public Data<?> cpuRead() {
        throw new UnsupportedOperationException("cpuRead - Unsupported Operation");
    }

    @Override
    public void cpuWrite(Data<?> data) {
        throw new UnsupportedOperationException("cpuWrite - Unsupported Operation");
    }

    @Override
    public void enqueueMessage(Message message) {
        getQueue().add(message);
    }
}
