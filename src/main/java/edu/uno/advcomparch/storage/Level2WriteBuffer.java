package edu.uno.advcomparch.storage;

import edu.uno.advcomparch.controller.CacheBlock;
import lombok.Data;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

@Data
@Named
public class Level2WriteBuffer {

    //Purpose of write buffer is to store cache blocks being written from cache to the next level in the hierarchy
    //Thus - L1 write buffer stores blocks being written to L2
    //     - L2 write buffer stores blocks being written to RAM


    private List<CacheBlock> buffer;

    public Level2WriteBuffer() {
        //initialize the buffer?
        buffer = new ArrayList<>();
    }
}
