package edu.uno.advcomparch.controller;

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

}