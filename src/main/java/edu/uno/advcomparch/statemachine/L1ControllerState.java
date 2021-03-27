package edu.uno.advcomparch.statemachine;

public enum L1ControllerState {
    // The requested line is hit in L1.
    HIT,
    // The requested line is a miss in L1 and the line present in the slot is in clean state.
    MISSC,
    // The requested line is a miss in L1 and the line present is in dirty state.
    MISSD,
    // The requested line is a miss in L1 and there is a no line present in the slot.
    MISSI,
    // Waiting for data from L1 for Read.
    RDWAITD,
    // Waiting for data from L2 for Read.
    RDL2WAITD,
    // Waiting for Data from L2/L2 for Read.
    RD2WAITD,
    // Waiting for data from L1 and L2 for Read.
    RD1WAITD,
    // Wait for data from L2 for write.
    WRWAITD,
    // Write Allocation done.
    WRALLOC,
    // Waiting for data from L1/L2 for write.
    WRWAIT2D,
    // Waiting for data from L1 and L2 for write.
    WRWAIT1D,
    // Psuedo State to hang out for
    WRWAITDX
}