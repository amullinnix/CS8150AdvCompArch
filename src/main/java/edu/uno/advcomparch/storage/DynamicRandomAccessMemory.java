package edu.uno.advcomparch.storage;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

// Requirements:
//      Dual Ported - Indicates multiple simultaneous access
//      Linear Indexed - 128Kb
@Data
public class DynamicRandomAccessMemory<T> {

    // List for array indexing, could probably initialize to size for 128Kb requirement
    private List<T> memory = new ArrayList<>();

}
