package edu.uno.advcomparch.storage;

import edu.uno.advcomparch.controller.Address;
import edu.uno.advcomparch.controller.CacheBlock;
import lombok.Data;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Requirements:
//      Dual Ported - Indicates multiple simultaneous access
//      Linear Indexed - 128Kb
@Data
@Named
public class DynamicRandomAccessMemory {

    private static final int MAIN_MEMORY_SIZE_IN_BYTES = 131072;  //128KB

    //TODO: Decide on which implementation is best
    private byte[] memory;

    public DynamicRandomAccessMemory() {
        memory = new byte[MAIN_MEMORY_SIZE_IN_BYTES];
    }

    // List for array indexing, could probably initialize to size for 128Kb requirement

    public CacheBlock getMemoryAtAddress(Address address) {
        String blockReference = zeroOutOffset(address);

        int startOfBlock = Integer.parseInt(blockReference, 2);
        int endOfBlock = startOfBlock + 32;

        byte[] bytes = Arrays.copyOfRange(memory, startOfBlock, endOfBlock);

        CacheBlock block = new CacheBlock(6, 32);
        block.setAddress(address);
        block.setBlock(bytes);

        return block;
    }

    //TODO: consider validation on the two addresses? Or do we even need to pass address?
    public void writeToRam(Address address, CacheBlock cacheBlock) {
        String blockReference = zeroOutOffset(address);

        int startOfBlock = Integer.parseInt(blockReference, 2);

        System.arraycopy(cacheBlock.getBlock(), 0, memory, startOfBlock, 32);

    }

    public void printBlock(Address address) {
        String blockReference = zeroOutOffset(address);

        int startOfBlock = Integer.parseInt(blockReference, 2);
        int endOfBlock = startOfBlock + 32;

        byte[] bytes = Arrays.copyOfRange(memory, startOfBlock, endOfBlock);

        System.out.println(Arrays.toString(bytes));
    }


    private String zeroOutOffset(Address address) {
        String addressAsString = address.getAddress();

        String addressMinusOffset = addressAsString.substring(0, addressAsString.length() - 5);

        return addressMinusOffset + "00000";
    }
}
