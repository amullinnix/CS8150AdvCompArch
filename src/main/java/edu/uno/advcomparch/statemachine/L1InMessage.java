package edu.uno.advcomparch.statemachine;

import edu.uno.advcomparch.controller.DataResponseType;

public enum L1InMessage {
    CPUREAD,
    DATA,
    CPUWRITE,
    // L1 Data Repository Messages
    MISSI,
    MISSC,
    MISSD;

    public static L1InMessage fromControllerState(DataResponseType dataResponseType) {
        try {
            return L1InMessage.valueOf(dataResponseType.toString());
        } catch (IllegalArgumentException exception) {
            System.out.println("Failed to Translate DataResponse to L1InMessage");
            return null;
        }
    }
}
