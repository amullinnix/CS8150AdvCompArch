package edu.uno.advcomparch.storage;

import edu.uno.advcomparch.controller.CacheBlock;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class VictimCache {

    //Purpose of the victim cache is to store the cache blocks that have been victimize from Level 1 Controller/Cache

    //maybe this is a has-a relationship?

    private List<CacheBlock> cache;

    public VictimCache() {
        this.cache = new ArrayList<>();
    }

}
