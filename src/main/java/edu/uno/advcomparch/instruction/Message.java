package edu.uno.advcomparch.instruction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {

    private Instruction instruction;

    //CPU? L1C? L2C? etc?
    //TODO: These might become enums later
    private String source;

    private String destination;
}
