package edu.uno.advcomparch.controller;

import org.checkerframework.checker.units.qual.C;
import org.junit.Before;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CacheBlockTest {

    private CacheBlock sut;

    @Before
    public void setup() {
        sut = new CacheBlock(6, 32);
    }

    @Test
    public void newBlockIsEmpty() {
         assertEquals(true, sut.isEmpty());
    }

    @Test
    public void blockWithDataIsNotEmpty() {
        Address address = new Address("101", "010", "001");

        sut.setAddress(address);
        sut.setBlock(new byte[] {4, 5, 6});

        assertEquals(false, sut.isEmpty());
    }

    @Test
    public void getTagAsString() {
        Address address = new Address("1010", "010", "001");

        sut.setAddress(address);

        assertEquals("1010", sut.getTagString());
    }

    @Test
    public void ensureAddressCopiedCorrectly() {
        Address address = new Address("1010", "010", "001");

        sut.setAddress(address);

        address.setTag("1111");

        assertEquals("1010", sut.getAddress().getTag());
    }

    @Test
    public void ensureByteArrayCopiedCorrectly() {
        Address address = new Address("1010", "010", "001");

        CacheBlock block = new CacheBlock(4, 4);
        block.setAddress(address);
        block.setBlock(new byte[]{1,2,3,4});

        CacheBlock newBlock = new CacheBlock(block);

        //Now, reset something on original block
        block.getBlock()[2] = 9;

        assertEquals(3, newBlock.getBlock()[2]);
    }

}