package edu.uno.advcomparch.controller;

import edu.uno.advcomparch.model.Data;

// Requirements:
//      Unified, direct mapped, write-back, write-allocate same block size as L1 and 16KB size.
//      L2D is also dual-ported like L1.
//      L2 and L1 must support mutual inclusion policy, which means that if mutual inclusion is violated then you must do whatever is needed to restore it.
public class Level2Controller implements CacheController {

    @Override
    public Data<?> cpuRead() {
        throw new UnsupportedOperationException("cpuRead - Unsupported Operation");
    }

    @Override
    public void cpuWrite(Data<?> data) {
        throw new UnsupportedOperationException("cpuWrite - Unsupported Operation");
    }
}
