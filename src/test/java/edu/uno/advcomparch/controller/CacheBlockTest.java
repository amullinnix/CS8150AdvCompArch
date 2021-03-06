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
        sut.setTag(new byte[] {1, 2, 3});
        sut.setBlock(new byte[] {4, 5, 6});

        assertEquals(false, sut.isEmpty());
    }

    @Test
    public void getTagAsString() {
        sut.setTag(new byte[]{49,48,49,48});

        assertEquals("1010", sut.getTagString());
    }

}