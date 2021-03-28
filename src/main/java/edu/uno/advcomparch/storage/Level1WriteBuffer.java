package edu.uno.advcomparch.storage;

import edu.uno.advcomparch.controller.CacheBlock;
import lombok.Data;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

@Data
@Named
public class Level1WriteBuffer {

    //Purpose of write buffer is to store cache blocks being written from cache to the next level in the hierarchy
    //Thus - L1 write buffer stores blocks being written to L2
    //     - L2 write buffer stores blocks being written to RAM

    //TODO: This must include write merging


    private List<CacheBlock> buffer;

    public Level1WriteBuffer() {
        //initialize the buffer?
        buffer = new ArrayList<>();
    }

    //If the buffer already contains other modified blocks, we compare addresses. In case of a match, we
    //can merge the data.


}
