package edu.uno.advcomparch.statemachine;

import edu.uno.advcomparch.controller.DataResponseType;

public enum ControllerMessage {
    CPUREAD,
    DATA,
    CPUWRITE,
    // L1 Data Repository Messages
    MISSI,
    MISSC,
    MISSD;

    public static ControllerMessage fromControllerState(DataResponseType dataResponseType) {
        try {
            return ControllerMessage.valueOf(dataResponseType.toString());
        } catch (IllegalArgumentException exception) {
            System.out.println("Failed to Translate DataResponse to L1InMessage");
            return null;
        }
    }
}
