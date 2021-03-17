package edu.uno.advcomparch.storage;

import lombok.Data;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

// Requirements:
//      Dual Ported - Indicates multiple simultaneous access
//      Linear Indexed - 128Kb
@Data
@Named
public class DynamicRandomAccessMemory<T> {

    private static final int MAIN_MEMORY_SIZE_IN_BYTES = 131072;  //128KB

    //TODO: Decide on which implementation is best
    private byte[] memory;

    public DynamicRandomAccessMemory() {
        memory = new byte[MAIN_MEMORY_SIZE_IN_BYTES];
    }

    // List for array indexing, could probably initialize to size for 128Kb requirement
    private List<T> dram = new ArrayList<>();

}
