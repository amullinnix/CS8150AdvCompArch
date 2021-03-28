package edu.uno.advcomparch.storage;

import edu.uno.advcomparch.controller.Address;
import edu.uno.advcomparch.controller.CacheBlock;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class DynamicRandomAccessMemoryTest {

    private DynamicRandomAccessMemory ram;

    @Before
    public void setup() {
      ram = new DynamicRandomAccessMemory();
    }

    @Test
    public void writeToRamWorks() {

        Address address = new Address("101010", "101010", "10101");

        CacheBlock block = new CacheBlock(6, 32);
        block.setAddress(address);
        block.getBlock()[10] = 1;

        ram.writeToRam(address, block);

        ram.printBlock(address);

    }

    @Test
    public void getMemory() {
        Address address = new Address("101010", "101010", "10101");

        CacheBlock block = new CacheBlock(6, 32);
        block.setAddress(address);
        block.getBlock()[10] = 1;

        ram.writeToRam(address, block);

        CacheBlock memoryAtAddress = ram.getMemoryAtAddress(address);

        assertEquals("[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]",
                Arrays.toString(memoryAtAddress.getBlock()));
    }

}