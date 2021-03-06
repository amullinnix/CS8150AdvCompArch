package edu.uno.advcomparch.controller;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AddressTest {

    @Test
    public void getAllValuesAsDecimals() {
        Address address = new Address("101011", "111100", "10001");

        assertEquals(43, address.getTagDecimal());
        assertEquals(60, address.getIndexDecimal());
        assertEquals(17, address.getOffsetDecimal());
    }

    @Test
    public void makeSureZeroPadWorks() {
        Address address = new Address("001011", "011100", "00001");

        assertEquals(11, address.getTagDecimal());
        assertEquals(28, address.getIndexDecimal());
        assertEquals(1, address.getOffsetDecimal());
    }

    @Test
    public void incrementOffsetByOne() {
        Address address = new Address("101011", "111100", "10001");

        address.incrementOffset();

        assertEquals(18, address.getOffsetDecimal());
    }


}