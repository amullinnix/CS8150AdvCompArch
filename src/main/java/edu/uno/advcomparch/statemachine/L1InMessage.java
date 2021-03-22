package edu.uno.advcomparch.statemachine;

import edu.uno.advcomparch.repository.DataResponseType;

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

    public static L1InMessage fromDataResponseType(DataResponseType dataResponseType) {
        try {
            return L1InMessage.valueOf(dataResponseType.toString());
        } catch (IllegalArgumentException exception) {
            System.out.println("Failed to Translate DataResponse to L1InMessage");
            return null;
        }
    }
}
