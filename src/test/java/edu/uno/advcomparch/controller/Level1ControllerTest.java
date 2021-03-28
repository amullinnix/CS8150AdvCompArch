package edu.uno.advcomparch.controller;

import edu.uno.advcomparch.AbstractCompArchTest;
import edu.uno.advcomparch.config.SimpleTestConfiguration;
import edu.uno.advcomparch.storage.Level1DataStore;
import edu.uno.advcomparch.storage.VictimCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ContextConfiguration(classes = SimpleTestConfiguration.class)
public class Level1ControllerTest extends AbstractCompArchTest {

    private Level1Controller level1Controller;

    @Autowired
    public VictimCache l1VictimCache;

    @BeforeEach
    public void setup() {

        l1VictimCache.getCache().clear();

        level1Controller = new Level1Controller(new LinkedBlockingQueue<>());
        level1Controller.setDataStore( new Level1DataStore(l1VictimCache) );
    }

    @Test
    public void victimizationWorks() {
        //fill up the cache
        Level1DataStore dataStore = level1Controller.getDataStore();
//        VictimCache victimCache = level1Controller.getVictimCache();

        assertEquals(true, l1VictimCache.getCache().isEmpty());

        Address address = new Address("000001", "000100", "00100");
        byte b = 1;

        populateCache(dataStore, address, b);

        //write to the full cache set (using the Level 1 Controller)
        address.setTag("000101");
        level1Controller.write(address, b);

        //victim buffer should have an entry, specifically the LRU cache block that was evicted
        assertEquals(1, l1VictimCache.getCache().size());
        assertEquals("000001", new String(l1VictimCache.getCache().get(0).getTag()));

        //optionally consider the miss state here
    }

    private void populateCache(Level1DataStore dataStore, Address address, byte b) {
        dataStore.writeDataToCache(address, b);

        address.setTag("000010");
        dataStore.writeDataToCache(address, b);

        address.setTag("000011");
        dataStore.writeDataToCache(address, b);

        address.setTag("000100");
        dataStore.writeDataToCache(address, b);

        dataStore.printSingleSet(4);
    }

}