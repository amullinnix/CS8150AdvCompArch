package edu.uno.advcomparch.utility;

import edu.uno.advcomparch.AbstractCompArchTest;
import edu.uno.advcomparch.instruction.InstructionType;
import org.junit.jupiter.api.Test;
import java.io.FileNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;  // main one
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class InstructionFileReaderUtilityTest extends AbstractCompArchTest {

    @Test
    public void testRead() throws FileNotFoundException {
        var instructions = InstructionFileReaderUtility.readInstruction("./src/test/resources/instructionsTest.txt");

        assertThat(instructions).hasSize(2);

        assertThat(instructions.get(0).getType()).isEqualTo(InstructionType.CPURead);
        assertThat(instructions.get(0).getAddress()).isEqualTo("A");
        assertThat(instructions.get(0).getSource()).isEqualTo(1);

        assertThat(instructions.get(1).getType()).isEqualTo(InstructionType.CPUWrite);
        assertThat(instructions.get(1).getAddress()).isEqualTo("B");
        assertThat(instructions.get(1).getSource()).isEqualTo(2);
    }

    @Test()
    public void testReadInstructionNotFound() {
        assertThatExceptionOfType(FileNotFoundException.class)
                .isThrownBy(() -> InstructionFileReaderUtility.readInstruction("blargh"))
                .withMessage("blargh (The system cannot find the file specified)");
    }
}
