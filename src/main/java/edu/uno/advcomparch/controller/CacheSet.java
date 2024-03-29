package edu.uno.advcomparch.controller;

import lombok.Data;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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

        CacheBlock lru = this.getLeastRecentlyUsedBlock();

        //what the hell is even going on here
        if( lru != null && lru.isEmpty()){
            this.blocks.remove(lru);
        } else if (atCapacity()) {
            evicted = lru;
            this.blocks.remove(lru);
        }

        blocks.addFirst(block);

        return evicted;
    }

    public boolean atCapacity() {

        List<CacheBlock> nonEmptyBlocks = blocks
                .stream()
                .filter(block -> !block.isEmpty())
                .collect(Collectors.toList());

        return nonEmptyBlocks.size() >= capacity;
    }

    //NOTE: this is like a peek, it doesn't adjust the LRU values
    public CacheBlock getMostRecentlyUsedBlock() {
        CacheBlock firstBlock = blocks.getFirst();

        return firstBlock;
    }

    //NOTE: this is like a peek, it doesn't adjust the LRU values
    public CacheBlock getLeastRecentlyUsedBlock() {

        if(blocks.size() > 0) {
            return this.blocks.getLast();
        } else {
            return null;
        }
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
                this.blocks.addFirst(blockFound);  //...and add it to the head (as the most recently used block)
                break;
            }
        }

        return blockFound;
    }

    //TODO: Yikes! What was I thinking? Do we like this? Feels weird, but seems to work. (see the whatHappens test)
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

    public boolean containsTag(Address address) {

        byte[] tagBytes = address.getTag().getBytes();

        Iterator<CacheBlock> iterator = this.blocks.iterator();

        while(iterator.hasNext()) {
            CacheBlock cacheBlock = iterator.next();
            if(Arrays.equals(cacheBlock.getTag(), tagBytes)) {
                return true;
            }
        }

        return false;
    }


    public void print() {

        for(CacheBlock block : this.blocks) {
            System.out.println(block);
        }
    }
}
