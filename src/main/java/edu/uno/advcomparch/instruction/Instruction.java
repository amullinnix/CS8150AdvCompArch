package edu.uno.advcomparch.instruction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Instruction {

    private InstructionType type;

    private String address;

    //CPU? L1C? L2C? etc?
    private int source;
}
