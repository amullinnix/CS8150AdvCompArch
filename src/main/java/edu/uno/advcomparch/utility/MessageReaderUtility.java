package edu.uno.advcomparch.utility;

import edu.uno.advcomparch.cpu.CPUMessageType;
import edu.uno.advcomparch.statemachine.ControllerMessage;
import lombok.experimental.UtilityClass;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@UtilityClass
public class MessageReaderUtility {

    public List<Message<ControllerMessage>> readMessages(String filename) throws FileNotFoundException {
        var messages = new ArrayList<Message<ControllerMessage>>();

        var messageFile = new File(filename);
        var messageScanner = new Scanner(messageFile);

        while (messageScanner.hasNextLine()) {
            var messageString = messageScanner.nextLine();
            var messageSplit = messageString.split("\\s+");
            var cpuMessageType = CPUMessageType.getEnum(messageSplit[0]);

            if (cpuMessageType == CPUMessageType.CPU_READ) {
                messages.add(MessageBuilder.withPayload(ControllerMessage.CPUREAD)
                        .setHeader("source", "CPU")
                        .setHeader("address", messageSplit[1])
                        .setHeader("bytes", Integer.valueOf(messageSplit[2]))
                        .build());
            } else if (cpuMessageType == CPUMessageType.CPU_WRITE) {
                messages.add(MessageBuilder.withPayload(ControllerMessage.CPUWRITE)
                        .setHeader("source", "CPU")
                        .setHeader("address", messageSplit[1])
                        .setHeader("data", messageSplit[2])
                        .build());
            }
        }

        return messages;
    }

}
