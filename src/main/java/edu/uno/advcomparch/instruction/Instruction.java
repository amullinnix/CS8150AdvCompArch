package edu.uno.advcomparch.instruction;

import edu.uno.advcomparch.statemachine.L1InMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Instruction {

    private L1InMessage type;

    private String address;

    //TODO: This might be offset, instead?
    private String source;
}
