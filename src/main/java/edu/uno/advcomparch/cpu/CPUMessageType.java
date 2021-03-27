package edu.uno.advcomparch.cpu;

import java.util.Arrays;

public enum CPUMessageType {
    CPU_READ("CPURead"),
    CPU_WRITE("CPUWrite");

    public final String value;

    CPUMessageType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static CPUMessageType getEnum(String value) {
        return Arrays.stream(values())
                .filter(cpuMessageType -> cpuMessageType.getValue().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unrecognized CPUMessageType"));
    }
}
