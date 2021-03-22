package edu.uno.advcomparch.utility;

import edu.uno.advcomparch.instruction.Instruction;
import edu.uno.advcomparch.statemachine.L1InMessage;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@UtilityClass
public class InstructionFileReaderUtility {

    public List<Instruction> readInstructions(String filename) throws FileNotFoundException {
        var instructions = new ArrayList<Instruction>();

        var instructionFile = new File(filename);
        var instructionScanner = new Scanner(instructionFile);

        while (instructionScanner.hasNextLine()) {
            var instructionString = instructionScanner.nextLine();
            var instruction = instructionString.split("\\s+");
            var l1InMessage = L1InMessage.valueOf(instruction[0]);

            instructions.add(new Instruction(l1InMessage, instruction[1], instruction[2]));
        }

        return instructions;
    }

}
