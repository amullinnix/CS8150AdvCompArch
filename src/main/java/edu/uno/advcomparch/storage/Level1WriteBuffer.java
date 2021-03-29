package edu.uno.advcomparch.storage;

import edu.uno.advcomparch.controller.Address;
import edu.uno.advcomparch.controller.CacheBlock;
import lombok.Data;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    /**
     * This add method automatically does write merging. Remember, '0' is an empty value in a byte array
     */
    public void add(CacheBlock evicted) {

        Optional<CacheBlock> match = buffer
                .stream()
                .filter(block -> block.getAddress().equals(evicted.getAddress()))
                .findFirst();

        if(match.isPresent()) {
            byte[] evictedBlock = evicted.getBlock();
            byte[] matchedBlock = match.get().getBlock();

            for(int i = 0; i < evictedBlock.length; i++) {
                if(evictedBlock[i] != 0) {
                    matchedBlock[i] = evictedBlock[i];
                }
            }
        } else {
            buffer.add(evicted);
        }
    }

    public void printData() {

        buffer.stream().forEach(System.out::println);
    }

    public CacheBlock getData(Address address) {
        return buffer.stream()
                .filter(cacheBlock ->
                        cacheBlock.getAddress()
                                .getAddress().equals(address.getAddress()))
                .findFirst()
                .orElse(null);
    }

    //If the buffer already contains other modified blocks, we compare addresses. In case of a match, we
    //can merge the data.


}
