package edu.uno.advcomparch.controller;

import edu.uno.advcomparch.instruction.Instruction;
import edu.uno.advcomparch.instruction.Message;
import edu.uno.advcomparch.model.Data;

import java.lang.reflect.Array;
import java.util.*;

// Requirements:
//      Split, 4-way, write-back, write-allocate, 32 byte block size and size 8KB.
//      L1C must support valid, dirty bits.
//      Assume that L1D is dual-ported – one read and one write port.
@lombok.Data
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

    public boolean isDataPresentInCache(Address address) {
        //get the index to figure out the set
        String index = address.getIndex();
        int decimalIndex = Integer.parseInt(index, 2);

        //fetch that set
        CacheSet set = data.get(decimalIndex);

        //compare the tag to our address' tag
        String tag = address.getTag();
        byte [] tagBytes = tag.getBytes();
        System.out.println("Tag from Address: " + Arrays.toString(tagBytes));

        //less naive approach, check them all
        for(CacheBlock block : set.getBlocks()) {
            if(Arrays.equals(block.getTag(), tagBytes)) {
                return true;
            }
        }

        return false;
    }

    public void writeDataToCache(Address address, byte b) {
        //index tells the set we are using
        String index = address.getIndex().replaceFirst("^0+(?!$)", "");
        int decimalIndex = Integer.parseInt(index, 2);
        System.out.println("The decimal index is: " + decimalIndex);

        //fetch the set (with the 4 cache blocks)
        CacheSet set = data.get(decimalIndex);

        //less naive approach, find first empty cache block
        CacheBlock emptyBlock = null;

        for(CacheBlock block : set.getBlocks()) {
            if(block.isEmpty()) {   //TODO: Java 8+ ify this.
                System.out.println("empty block found!");
                emptyBlock = block;
                break;  //I hate breaks
            } else {
                System.out.println("block not empty :(");
            }
        }

        if(emptyBlock == null) {
            System.out.println("Set is full - oh noes!");
        }

        //must put tag in there
        String tag = address.getTag().replaceFirst("^0(?!$)", "");
        int decimalTag = Integer.parseInt(tag, 2);
        System.out.println("The decimal tag is: " + decimalTag);

        emptyBlock.setTag(address.getTag().getBytes());

        //now add the data using the offset
        String offset = address.getOffset();
        int decimalOffset = Integer.parseInt(offset, 2);
        System.out.println("The decimal offset is: " + decimalOffset);
        byte[] block = emptyBlock.getBlock();
        block[decimalOffset] = b;
        //do we need to set it back? We'll find out!
    }

    public byte getByteAtAddress(Address address) {
        //get the index to figure out the set
        String index = address.getIndex();
        int decimalIndex = Integer.parseInt(index, 2);

        //fetch that set
        CacheSet set = data.get(decimalIndex);

        //compare the tag to our address' tag
        String tag = address.getTag();
        byte [] tagBytes = tag.getBytes();
        System.out.println("Tag from Address: " + Arrays.toString(tagBytes));

        //find the matching block
        for(CacheBlock block : set.getBlocks()) {
            if(Arrays.equals(block.getTag(), tagBytes)) {
                String offset = address.getOffset();
                int decimalOffset = Integer.parseInt(offset, 2);
                System.out.println("The decimal offset is: " + decimalOffset);
                return block.getBlock()[decimalOffset];
            }
        }

        return 1;  //ugly
    }

    public byte[] getDataAtAddress(Address address, int i) {

        byte[] bytes = new byte[]{};

        return bytes;
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
}
