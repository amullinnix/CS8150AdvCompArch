package edu.uno.advcomparch.controller;

import org.junit.Test;

import java.nio.channels.UnsupportedAddressTypeException;

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

    @Test
    public void fullAddress() {
        Address address = new Address("101011", "111100", "10001");

        assertEquals("10101111110010001", address.getAddress());
    }

    @Test(expected = UnsupportedAddressTypeException.class)
    public void setAddressMustMaintainConsistency() {
        Address address = new Address("101011", "111100", "10001");

        address.setAddress("10101");
    }

    @Test
    public void getAddressMustReturnTheCorrectAddress() {
        Address address = new Address();
        address.setAddress("101010");

        assertEquals("101010", address.getAddress());
    }

    @Test
    public void splitAddressIntoParts() {
        Address address = new Address();
        address.setAddress("10101111110010001");

        address.componentize(6, 6, 5);

        assertEquals("101011", address.getTag());
        assertEquals("111100", address.getIndex());
        assertEquals("10001", address.getOffset());
    }

    @Test
    public void componentizationWorksForL2() {
        Address address = new Address();
        address.setAddress("10101111110010001");

        address.componentize(9, 0, 5);

        assertEquals("011111100", address.getTag());
        assertEquals("10001", address.getOffset());

    }

}