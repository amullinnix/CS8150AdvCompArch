package edu.uno.advcomparch.storage;

import edu.uno.advcomparch.controller.Address;
import edu.uno.advcomparch.controller.CacheBlock;
import lombok.Data;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@Named
public class VictimCache {

    //Purpose of the victim cache is to store the cache blocks that have been victimize from Level 1 Controller/Cache

    //maybe this is a has-a relationship?

    private List<CacheBlock> cache;

    public VictimCache() {
        this.cache = new ArrayList<>();
    }

    public byte[] getData(Address address, int bytesToRead) {
        //fetch the set
        CacheBlock block = cache.get(address.getIndexDecimal());

        int decimalOffset = address.getOffsetDecimal();

        return Arrays.copyOfRange(block.getBlock(), decimalOffset, decimalOffset + bytesToRead);
    }


}
