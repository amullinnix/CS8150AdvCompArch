package edu.uno.advcomparch.storage;

import lombok.Data;

import java.util.Map;

@Data
public class DynamicRandomAccessMemory {

    private Map<String, String> memory;

}
