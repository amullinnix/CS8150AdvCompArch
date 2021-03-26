package edu.uno.advcomparch.storage;

import edu.uno.advcomparch.controller.CacheBlock;

import java.util.List;

public class Level1WriteBuffer {

    //Purpose of write buffer is to store cache blocks being written from cache to the next level in the hierarchy
    //Thus - L1 write buffer stores blocks being written to L2
    //     - L2 write buffer stores blocks being written to RAM

    //TODO: This must include write merging


    private List<CacheBlock> buffer;

    public Level1WriteBuffer() {
        //initialize the buffer?
    }
}
