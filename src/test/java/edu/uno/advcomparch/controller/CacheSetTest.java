package edu.uno.advcomparch.controller;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class CacheSetTest {

    private CacheSet cacheSet;

    @Before
    public void setup() {
        cacheSet = new CacheSet(4);

    }

    @Test
    public void cacheSetHasCapacity() {
        assertEquals(4, cacheSet.getCapacity());

        //also, let's assert the capacity is not full
        assertFalse(cacheSet.atCapacity());
    }

    @Test
    public void tellMeWhenFull() {
        CacheBlock block1 = new CacheBlock(5, 32);
        block1.setBlock("abc".getBytes());
        block1.setTag("abc".getBytes());

        //just add the same block, it doesn't matter for this test
        cacheSet.add(block1);
        cacheSet.add(block1);
        cacheSet.add(block1);
        cacheSet.add(block1);

        assertTrue(cacheSet.atCapacity());
    }

    @Test
    public void firstAddIsHead() {
        //Essentially, every time we write something, it needs to be become the "first" because it's a recently used
        CacheBlock block1 = new CacheBlock(5, 32);
        block1.setTag(new byte[] {1,2,3,4,5});
        cacheSet.add(block1);

        CacheBlock block = cacheSet.getMostRecentlyUsedBlock();

        assertEquals(block1, block);
    }

    @Test
    public void secondAddIsHead() {
        //Essentially, every time we write something, it needs to be become the "first" because it's a recently used
        CacheBlock block1 = new CacheBlock(5, 32);
        block1.setTag(new byte[] {1,2,3,4,5});


        CacheBlock block2 = new CacheBlock(5, 32);
        block2.setTag(new byte[] {6,7,8,9,0});

        cacheSet.add(block1);
        cacheSet.add(block2);

        CacheBlock block = cacheSet.getMostRecentlyUsedBlock();

        assertEquals(2, cacheSet.getBlocks().size());
        assertEquals(block2, block);
    }

    @Test
    public void findBlock() {
        CacheBlock block1 = new CacheBlock(5, 32);
        block1.setTag("1010".getBytes());
        block1.setBlock("111".getBytes());

        cacheSet.add(block1);

        Address address = new Address("1010", "111", "100");

        CacheBlock block = cacheSet.getBlock(address);   //TODO: This damn well better reset the head.

        assertEquals(Arrays.toString(block1.getBlock()), Arrays.toString(block.getBlock()));
    }

    @Test
    public void accessResetsHead() {
        CacheBlock block1 = new CacheBlock(5, 32);
        block1.setTag("1010".getBytes());
        block1.setBlock("111".getBytes());

        CacheBlock block2 = new CacheBlock(5, 32);
        block2.setTag(new byte[] {6,7,8,9,0});

        cacheSet.add(block1);  //add first, it becomes head
        cacheSet.add(block2);  //write second, it becomes head

        //now access first again, it should become head!
        Address address = new Address("1010", "111", "100");
        cacheSet.getBlock(address);

        CacheBlock block = cacheSet.getMostRecentlyUsedBlock();

        assertEquals(block1, block);
        assertEquals(2, cacheSet.getBlocks().size());

        System.out.println(cacheSet);

    }

    @Test
    public void addingWhenAtCapacityBumpsOutLru() {
        cacheSet.setCapacity(2); //arbitrarily set capacity at 2

        CacheBlock block1 = new CacheBlock(5, 32);
        block1.setTag("1000".getBytes());
        block1.setBlock("111".getBytes());

        CacheBlock block2 = new CacheBlock(5, 32);
        block2.setTag("1001".getBytes());
        block2.setBlock("101".getBytes());

        CacheBlock block3 = new CacheBlock(5, 32);
        block3.setTag("1010".getBytes());
        block3.setBlock("100".getBytes());

        cacheSet.add(block1);  //add first, it becomes head
        cacheSet.add(block2);  //write second, it becomes head
        cacheSet.add(block3);  //block three should be head, block 1 should get bumped

        System.out.println(cacheSet);

        //should only be two elements
        assertEquals(2, cacheSet.getBlocks().size());

        //assert block 3
        CacheBlock block = cacheSet.getMostRecentlyUsedBlock();
        assertEquals(block3, block);

        //assert block 2
        block = cacheSet.getBlocks().get(1);
        assertEquals(block2, block);
    }

    @Test
    public void addingWhenAtCapacityReturnsLru() {
        cacheSet.setCapacity(2); //arbitrarily set capacity at 2

        CacheBlock block1 = new CacheBlock(5, 32);
        block1.setTag("1000".getBytes());
        block1.setBlock("111".getBytes());

        CacheBlock block2 = new CacheBlock(5, 32);
        block2.setTag("1001".getBytes());
        block2.setBlock("101".getBytes());

        CacheBlock block3 = new CacheBlock(5, 32);
        block3.setTag("1010".getBytes());
        block3.setBlock("100".getBytes());

        cacheSet.add(block1);  //add first, it becomes head
        cacheSet.add(block2);  //write second, it becomes head

        //When adding a third, we expect that "evicted" block to be handed back (so something can be done with it)
        CacheBlock evicted = cacheSet.add(block3);

        assertEquals(evicted, block1);
    }

    @Test
    public void removeBlock() {
        //Creating this test just in case we have the need to remove a specific block for some reason.

        CacheBlock block1 = new CacheBlock(5, 32);
        block1.setTag("1000".getBytes());
        block1.setBlock("111".getBytes());

        cacheSet.add(block1);
        assertEquals(1, cacheSet.getBlocks().size());

        Address address = new Address("1000", "", "101");
        boolean removed = cacheSet.removeBlock(address);

        assertTrue(removed);
        assertEquals(0, cacheSet.getBlocks().size());
    }

    @Test
    public void setContainsTag() {
        CacheBlock block1 = new CacheBlock(5, 32);
        block1.setTag("10001".getBytes());
        block1.setBlock("111".getBytes());

        cacheSet.add(block1);

        Address address = new Address("10001", "111", "100");

        assertTrue(cacheSet.containsTag(address));
    }

    @Test
    public void setDoesNotContainTag() {
        CacheBlock block1 = new CacheBlock(5, 32);
        block1.setTag("10001".getBytes());
        block1.setBlock("111".getBytes());

        cacheSet.add(block1);

        Address address = new Address("11111", "111", "100");

        assertFalse(cacheSet.containsTag(address));
    }

    @Test
    public void seeTheLeastRecentlyUsedBlock() {
        cacheSet.setCapacity(2); //arbitrarily set capacity at 2

        CacheBlock block1 = new CacheBlock(5, 32);
        block1.setTag("1000".getBytes());
        block1.setBlock("111".getBytes());

        CacheBlock block2 = new CacheBlock(5, 32);
        block2.setTag("1001".getBytes());
        block2.setBlock("101".getBytes());

        cacheSet.add(block1);  //add first, it becomes head
        cacheSet.add(block2);  //write second, it becomes head

        CacheBlock lruBlock = cacheSet.getLeastRecentlyUsedBlock();

        assertEquals("1000", new String(lruBlock.getTag()));
    }
}
