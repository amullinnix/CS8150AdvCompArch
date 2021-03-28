package edu.uno.advcomparch.storage;

import edu.uno.advcomparch.controller.Address;
import edu.uno.advcomparch.controller.CacheBlock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class VictimCacheTest {

    private VictimCache victimCache;

    @BeforeEach
    public void setUp() {
        victimCache = new VictimCache();

        var victimAddress = new Address(null);
        victimAddress.setAddress("101");

        var victimCacheBlock = new CacheBlock(4,4);
        victimCacheBlock.setAddress(victimAddress);

        victimCache.getCache().add(victimCacheBlock);
    }

    @Test
    public void testFindsCacheBlock() {
        assertThat(victimCache.getData(new Address("101"))).isNotNull();
    }

    @Test
    public void testDoNotFindCacheBlock() {
        assertThat(victimCache.getData(new Address("102"))).isNull();
    }
}
