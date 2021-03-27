package edu.uno.advcomparch.controller;

public enum DataResponseType {
    // The request line is a hit in L1.
    HIT,
    // The requested line is a miss in L1 and the line present in the slot is in a clean state.
    MISSC,
    // The requested line is a miss in L1 and the line present is in a dirty state.
    MISSD,
    // The requested line is a miss in L1 and there is no line present in the slot.
    MISSI


    /**
     * A note on cache misses. There are three C's, and we ain't talking about diamonds
     *
     *  Compulsory - very first access to cache, must be empty, duh
     *
     *  Capacity - cache is full. No room at the inn
     *
     *  Conflict - multiple blocks map to the same location in the cache
     *
     *  We lied, there's a fourth C - coherence
     *
     */
}
