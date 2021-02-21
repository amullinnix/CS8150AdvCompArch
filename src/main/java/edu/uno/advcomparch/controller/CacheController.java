package edu.uno.advcomparch.controller;

import edu.uno.advcomparch.model.Data;

public interface CacheController {

    // service a read request from the CPU.
    Data<?> cpuRead();

    // service a write request from the CPU.
    void cpuWrite(Data<?> data);

}
