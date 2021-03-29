package edu.uno.advcomparch.utility;

import edu.uno.advcomparch.AbstractCompArchTest;
import edu.uno.advcomparch.config.L1StateMachineTestConfiguration;
import edu.uno.advcomparch.statemachine.ControllerMessage;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

import java.io.FileNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ContextConfiguration(classes = L1StateMachineTestConfiguration.class)
public class MessageReaderUtilityTest extends AbstractCompArchTest {

    @Test
    public void testRead() throws FileNotFoundException {
        var messages = MessageReaderUtility.readMessages("./src/test/resources/MessagesTest.txt");

        assertThat(messages).hasSize(2);

        // Test CPU Read
        assertThat(messages.get(0)).isNotNull();
        var cpuRead = messages.get(0);

        assertThat(cpuRead.getPayload()).isEqualTo(ControllerMessage.CPUREAD);

        assertThat(cpuRead.getHeaders()).isNotEmpty();
        var cpuReadHeaders = cpuRead.getHeaders();

        assertThat(cpuReadHeaders.get("address")).isEqualTo("A");
        assertThat(cpuReadHeaders.get("bytes")).isEqualTo(1);

        // Test CPU Write
        assertThat(messages.get(1)).isNotNull();
        var cpuWrite = messages.get(1);

        assertThat(cpuWrite.getPayload()).isEqualTo(ControllerMessage.CPUWRITE);

        assertThat(cpuWrite.getHeaders()).isNotEmpty();
        var cpuWriteHeaders = cpuWrite.getHeaders();

        assertThat(cpuWriteHeaders.get("address")).isEqualTo("B");
        assertThat(cpuWriteHeaders.get("data")).isEqualTo("2");
    }

    @Test()
    public void testReadInstructionNotFound() {
        assertThatExceptionOfType(FileNotFoundException.class)
                .isThrownBy(() -> MessageReaderUtility.readMessages("blargh"))
                .withMessage("blargh (The system cannot find the file specified)");
    }
}
