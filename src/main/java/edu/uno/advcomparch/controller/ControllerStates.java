package edu.uno.advcomparch.controller;

public enum ControllerStates {
    // The request line is a hit in L1.
    HIT,
    // The requested line is a miss in L1 and the line present in the slot is in a clean state.
    MISC,
    // The requested line is a miss in L1 and the line present is in a dirty state.
    MISSD,
    // The requested line is a miss in L1 and there is no line present in the slot.
    MISSI
}
