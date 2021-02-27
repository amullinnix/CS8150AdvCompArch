package edu.uno.advcomparch.instruction;

import edu.uno.advcomparch.model.Controller;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {

    private Instruction instruction;

    private Controller source;

    private Controller destination;
}
