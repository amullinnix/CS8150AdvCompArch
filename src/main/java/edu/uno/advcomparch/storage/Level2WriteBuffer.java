package edu.uno.advcomparch.storage;

import edu.uno.advcomparch.controller.CacheBlock;
import lombok.Data;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    /**
     * For Level 2, there really isn't write merging, as we deal in whole Cache Blocks only
     */
    public void add(CacheBlock evicted) {

        CacheBlock blockToAdd = new CacheBlock(9, 32);

        blockToAdd.setAddress(evicted.getAddress());
        blockToAdd.setBlock(Arrays.copyOf(evicted.getBlock(), evicted.getBlock().length));

        buffer.add(blockToAdd);

    }

    public void printData() {

        buffer.stream().forEach(System.out::println);
    }
}
