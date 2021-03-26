package edu.uno.advcomparch.statemachine;

import edu.uno.advcomparch.controller.ControllerState;

public enum L1InMessage {
    START,
    END,
    CPUREAD,
    DATA,
    CPUWRITE,
    // L1 Data Repository Messages
    MISSI,
    MISSC,
    MISSD;

    public static L1InMessage fromControllerState(ControllerState controllerState) {
        try {
            return L1InMessage.valueOf(controllerState.toString());
        } catch (IllegalArgumentException exception) {
            System.out.println("Failed to Translate DataResponse to L1InMessage");
            return null;
        }
    }
}
