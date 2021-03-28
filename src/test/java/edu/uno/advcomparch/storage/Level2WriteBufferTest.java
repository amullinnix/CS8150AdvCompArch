package edu.uno.advcomparch.storage;

import edu.uno.advcomparch.AbstractCompArchTest;
import edu.uno.advcomparch.config.SimpleTestConfiguration;
import edu.uno.advcomparch.controller.Address;
import edu.uno.advcomparch.controller.CacheBlock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ContextConfiguration(classes = SimpleTestConfiguration.class)
public class Level2WriteBufferTest extends AbstractCompArchTest {

    private Level2WriteBuffer buffer;

    @BeforeEach
    public void setup() {
        buffer = new Level2WriteBuffer();
    }

    @Test
    public void writeMergingWorks() {
        Address address = new Address("00010000");
        address.componentize(4, 2, 2);

        byte b1 = 1;
        byte b2 = 2;

        CacheBlock block1 = new CacheBlock(4, 6);
        block1.setAddress(address);
        block1.getBlock()[0] = b1;

        CacheBlock block2 = new CacheBlock(4, 6);
        block2.setAddress(address);
        block2.getBlock()[2] = b2;

        buffer.add(block1);
        buffer.add(block2);

        buffer.printData();

        assertEquals(1, buffer.getBuffer().size());
        assertTrue(Arrays.equals(new byte[]{1,0,2,0,0,0}, buffer.getBuffer().get(0).getBlock()));
    }
}