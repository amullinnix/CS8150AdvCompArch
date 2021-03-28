package edu.uno.advcomparch.storage;

import edu.uno.advcomparch.controller.Address;
import edu.uno.advcomparch.controller.CacheBlock;
import edu.uno.advcomparch.controller.CacheSet;
import edu.uno.advcomparch.controller.DataResponseType;
import lombok.Getter;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//Purpose of this class to simply handle the requests from the Level 1 Controller
@Getter
@Named
public class Level1DataStore {

    //TODO: Consider calculating this and extracting as well
    public static final int NUMBER_OF_SETS = 64;
    public static final int NUMBER_OF_BLOCKS = 4;
    public static final int TAG_SIZE = 6;
    public static final int BLOCK_SIZE = 32;

    private List<CacheSet> cache;
    private final VictimCache victimCache;

    @Inject
    public Level1DataStore(VictimCache l1VictimCache) {
        this.victimCache = l1VictimCache;
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
    public DataResponseType isDataPresentInCache(Address address) {

        //fetch the set
        CacheSet set = cache.get(address.getIndexDecimal());

        CacheBlock block = set.getBlock(address);

        return this.canWriteToCache(address);
//        if(block.isEmpty()) {
//            return null;
//        } else {
//            return null;
//        }
    }

    public void writeDataToCacheTriggeredByRead(Address address, byte[] bytesToWrite) {

        for(int i = 0; i < bytesToWrite.length; i++) {
            this.writeDataToCacheTriggeredByRead(address, bytesToWrite[i]);

            //must increment the offset by one to write the next byte in the correct location
            address.incrementOffset();
        }
    }

    public void writeDataToCacheTriggeredByRead(Address address, byte b) {
        writeDataToCache(address, b);

        //easy way of doing this ;)
        this.getCacheBlock(address).setDirty(false);
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

        CacheBlock evicted = null;

        //fetch the set (with the 4 cache blocks)
        CacheSet set = cache.get(address.getIndexDecimal());

        if(isDataPresentInCache(address) == DataResponseType.HIT) {
            //then we are updating it, kind of
            CacheBlock block = set.getBlock(address);

            block.setAddress(address);  //might be unnecessary? follow up.
            block.getBlock()[address.getOffsetDecimal()] = b;

        } else {
            CacheBlock block = new CacheBlock(TAG_SIZE, BLOCK_SIZE);

            block.setAddress(address);
            block.getBlock()[address.getOffsetDecimal()] = b;
            block.setDirty(true);
            block.setValid(true);

            evicted = set.add(block); //this returns the evicted block if there is one  //TODO: ugly :(
        }

        if (evicted != null) {
            victimCache.getCache().add(evicted);
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


    //TODO: OMG! So many freaking returns. Sonar is having a fit right now.
    public DataResponseType canWriteToCache(Address address) {
        if (address == null) {
            System.out.println("Cannot write null address to cache");
            return null;
        }

        CacheSet set = cache.get(address.getIndexDecimal());

        if(set.containsTag(address)) {
            return DataResponseType.HIT;                   //ok to write to full set, but same tag
        }else if(set.atCapacity()){
            CacheBlock leastRecentlyUsedBlock = set.getLeastRecentlyUsedBlock();
            if(leastRecentlyUsedBlock.isDirty()) {
                return DataResponseType.MISSD;
            } else {
                return DataResponseType.MISSC;                  //not ok, because cache is full and we have a new tag
            }
        } else {
            return DataResponseType.MISSI;                   //ok because there is room at the inn
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
            System.out.print("t: \"" + new String(block.getTag()) + "\" ");
            System.out.print("d: " + block.isDirty() + " ");
            System.out.print("v: " + block.isValid() + " ");
            System.out.print("b: " + Arrays.toString(block.getBlock()));
            System.out.println();
        }
    }

    public CacheSet getCacheSet(Address address) {

        CacheSet cacheSet = cache.get(address.getIndexDecimal());

        return cacheSet;
    }
}
