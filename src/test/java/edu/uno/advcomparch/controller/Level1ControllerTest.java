package edu.uno.advcomparch.controller;

import edu.uno.advcomparch.instruction.Instruction;
import edu.uno.advcomparch.instruction.InstructionType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class Level1ControllerTest {

    private Level1Controller controller;

    @Before
    public void setup() {
        controller = new Level1Controller();

        Address address = new Address();
        address.setTag("A");

        controller.getLevel1Data().put(address, "someData");
    }

    @Test
    public void doFirstRead() {

        Instruction instruction = new Instruction();
        instruction.setType(InstructionType.CPURead);
        instruction.setAddress("A");

        //execute
        controller.processInstruction(instruction);

        //assert something here?
        assertTrue(controller.getMessageList().contains("Address A found, returning someData"));
    }

}