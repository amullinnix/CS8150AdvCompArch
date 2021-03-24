package edu.uno.advcomparch.storage;

import edu.uno.advcomparch.controller.Address;
import edu.uno.advcomparch.controller.CacheBlock;
import edu.uno.advcomparch.controller.CacheSet;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//Purpose of this class to simply handle the requests from the Level 1 Controller
@Named
public class Level1DataStore {

    //TODO: Consider calculating this and extracting as well
    public static final int NUMBER_OF_SETS = 64;
    public static final int NUMBER_OF_BLOCKS = 4;
    public static final int TAG_SIZE = 6;
    public static final int BLOCK_SIZE = 32;

    private List<CacheSet> cache;

    public Level1DataStore() {
        //Initialize the cache! (data store)
        cache = new ArrayList<>();
        for(int i = 0; i < NUMBER_OF_SETS; i++) {
            CacheSet set = new CacheSet(NUMBER_OF_BLOCKS);
            for(int j = 0; j < NUMBER_OF_BLOCKS; j++) {
                CacheBlock block = new CacheBlock(TAG_SIZE, BLOCK_SIZE);
                set.add(block);
            }
            cache.add(set);
        }
    }

    //TODO: maybe this should be more tag focused?
    public boolean isDataPresentInCache(Address address) {

        //fetch the set
        CacheSet set = cache.get(address.getIndexDecimal());

        CacheBlock block = set.getBlock(address);

        if(block.isEmpty()) {
            return false;
        } else {
            return true;
        }
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
        CacheSet set = cache.get(address.getIndexDecimal());

        if(isDataPresentInCache(address)) {
            //then we are updating it, kind of
            CacheBlock block = set.getBlock(address);

            block.setTag(address.getTag().getBytes());  //this might be unnecessary later
            block.getBlock()[address.getOffsetDecimal()] = b;

        } else {
            CacheBlock block = new CacheBlock(TAG_SIZE, BLOCK_SIZE);

            block.setTag(address.getTag().getBytes());
            block.getBlock()[address.getOffsetDecimal()] = b;

            set.add(block);
        }
    }

    public byte getByteAtAddress(Address address) {

        //fetch the set
        CacheSet set = cache.get(address.getIndexDecimal());

        CacheBlock block = set.getBlock(address);

        return block.getBlock()[address.getOffsetDecimal()];
    }

    public byte[] getDataAtAddress(Address address, int bytesToRead) {

        //fetch the set
        CacheSet set = cache.get(address.getIndexDecimal());

        CacheBlock block = set.getBlock(address);

        int decimalOffset = address.getOffsetDecimal();

        return Arrays.copyOfRange(block.getBlock(), decimalOffset, decimalOffset + bytesToRead);
    }

    public boolean canWriteToCache(Address address) {
        if (address == null) {
            System.out.println("Cannot write null address to cache");
            return false;
        }

        CacheSet set = cache.get(address.getIndexDecimal());

        if(set.containsTag(address)) {
            return true;                   //ok to write to full set, but same tag
        }else if(set.atCapacity()){
            return false;                  //not ok, because cache is full and we have a new tag
        } else {
            return true;                   //ok because there is room at the inn
        }
    }

    public CacheBlock getCacheBlock(Address address) {
        CacheSet cacheSet = cache.get(address.getIndexDecimal());

        return cacheSet.getBlock(address);
    }

    public void printData() {

        System.out.println("Printing data of length: " + cache.size());

        for(int i = 0; i < cache.size(); i++) {
            printSingleSet(i);
            System.out.println();
        }
    }

    public void printSingleSet(int setToPrint) {

        CacheSet set = cache.get(setToPrint);
        System.out.println("Set " + setToPrint + ": ");

        List<CacheBlock> blocks = set.getBlocks();
        for(CacheBlock block : blocks) {
            System.out.print("t: " + Arrays.toString(block.getTag()));
            System.out.print("b: " + Arrays.toString(block.getBlock()));
            System.out.println();
        }
    }
}
