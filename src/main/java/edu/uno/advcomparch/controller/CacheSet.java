package edu.uno.advcomparch.controller;

import lombok.Data;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

//I'm feeling like this class is kind of useless atm. We'll see.
@Data
public class CacheSet {

    private LinkedList<CacheBlock> blocks;

    private int capacity;

    public CacheSet(int capacity) {
        this.blocks = new LinkedList<>();
        this.capacity = capacity;
    }

    /**
     * Basically, a write (add) always succeeds. However, sometimes, the set might be full. In that case, we
     * just return the Least Recently Used cache block as the evicted block. It's up to you to do something with it ;)
     */
    public CacheBlock add(CacheBlock block) {
        CacheBlock evicted = null;

        if(this.blocks.size() >= this.capacity) {
            evicted = this.blocks.removeLast();
        }

        blocks.addFirst(block);

        return evicted;
    }

    public boolean atCapacity() {
        return blocks.size() >= capacity;
    }

    public CacheBlock getMostRecentlyUsedBlock() {
        CacheBlock firstBlock = blocks.getFirst();

        return firstBlock;
    }

    //TODO: I wonder if this should take just a tag, you know?
    public CacheBlock getBlock(Address address) {

        CacheBlock blockFound = null;

        byte[] tagBytes = address.getTag().getBytes();

        Iterator<CacheBlock> iterator = this.blocks.iterator();

        while(iterator.hasNext()) {
            blockFound = iterator.next();
            if(Arrays.equals(blockFound.getTag(), tagBytes)) {
                iterator.remove();  //remove it from it's current location...
                break;
            }
        }

        //...and add it to the head (as the most recently used block)
        this.blocks.addFirst(blockFound);

        return blockFound;
    }

    public boolean removeBlock(Address address) {

        boolean removed = false;
        byte[] tagBytes = address.getTag().getBytes();

        Iterator<CacheBlock> iterator = this.blocks.iterator();

        while(iterator.hasNext()) {
            CacheBlock block = iterator.next();
            if(Arrays.equals(block.getTag(), tagBytes)) {
                iterator.remove();
                removed = true;
            }
        }

        return removed;
    }
}
