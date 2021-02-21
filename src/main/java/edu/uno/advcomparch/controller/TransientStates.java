package edu.uno.advcomparch.controller;

public enum TransientStates {
    // Waiting for data from L1 for read.
    RD_WAIT_D,
    // Waiting for data from L2 for read.
    RD_WAIT_L2_D,
    // Waiting for data from L1/L2 for read.
    RD_WAIT_1D,
    // Waiting for data from L1 and L2 for read.
    RD_WAIT_2D,
    // Waiting for data from L2 for write.
    WR_WAIT_D,
    // Waiting for data from L1/L2 for write.
    WR_WAIT_1D,
    // Waiting for data from L1 and L2 for write.
    WR_WAIT_2D,
    // Write Allocation done.
    WR_ALLOC
}
