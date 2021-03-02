package edu.uno.advcomparch.controller;

import edu.uno.advcomparch.instruction.Instruction;
import edu.uno.advcomparch.instruction.Message;
import edu.uno.advcomparch.model.Data;

import java.util.*;

// Requirements:
//      Split, 4-way, write-back, write-allocate, 32 byte block size and size 8KB.
//      L1C must support valid, dirty bits.
//      Assume that L1D is dual-ported – one read and one write port.
@lombok.Data
public class Level1Controller implements CacheController {


    private byte[][][] data;

    private List<String> messageList;

    private Queue<Message> queue;

    public Level1Controller(Queue<Message> queue) {

        data = new byte[64][][];
        for(int j = 0; j < 64; j++) {

            byte[][] set = new byte[4][];
            for (int i = 0; i < 4; i++) {  //put 4 cache blocks in one set
                byte[] cacheBlock = new byte[32+6];  //cache block size + tag size
                set[i] = cacheBlock;
            }

            data[j] = set;
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
        byte[][] set = data[decimalIndex];

        //naive approach, just check the first one
        byte[] cacheBlock = set[0];

        //get the tag, or the first 6 bytes
        byte[] tagFromCache = Arrays.copyOfRange(cacheBlock, 0, 6);
        System.out.println("Tag from Cache: " + Arrays.toString(tagFromCache));

        //compare the tag to our address' tag
        String tag = address.getTag();
        byte [] tagBytes = tag.getBytes();
        System.out.println("Tag from Address: " + Arrays.toString(tagBytes));

        return (Arrays.equals(tagFromCache, tagBytes));

    }

    public void writeDataToCache(Address address, byte b) {
        //index tells the set we are using
        String index = address.getIndex();
        int decimalIndex = Integer.parseInt(index, 2);
        System.out.println("The decimal index is: " + decimalIndex);

        //fetch the set (with the 4 cache blocks)
        byte[][] set = data[decimalIndex];

        //naive approach, just stick in first entry for now
        byte[] cacheBlock = set[0];

        //must put tag in there
        String tag = address.getTag();
        int decimalTag = Integer.parseInt(tag, 2);
        System.out.println("The decimal tag is: " + decimalTag);
        for(int i = 0; i < tag.length(); i++) {
            cacheBlock[i] = (byte) tag.charAt(i);
        }

        //now add the data using the offset
        String offset = address.getOffset();
        int decimalOffset = Integer.parseInt(offset, 2);
        System.out.println("The decimal offset is: " + decimalOffset);
        cacheBlock[decimalOffset + 5] = b;  //add the tag size - why 5 and not 6?

    }

    public void printData() {

        System.out.println("Printing data of length: " + data.length);
        for(int i = 0; i < data.length; i++) {
            System.out.print("Set " + i + ": ");
            byte[][] set = data[i];
            for(int j = 0; j < set.length; j++) {
                byte[] block = set[j];
                System.out.print(Arrays.toString(block));
            }
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
