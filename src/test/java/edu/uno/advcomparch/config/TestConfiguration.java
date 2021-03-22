package edu.uno.advcomparch.config;

import edu.uno.advcomparch.controller.Level1Controller;
import edu.uno.advcomparch.controller.Level2Controller;
import edu.uno.advcomparch.cpu.CentralProcessingUnit;
import edu.uno.advcomparch.cpu.DefaultCPU;
import edu.uno.advcomparch.instruction.Message;
import edu.uno.advcomparch.repository.DataRepository;
import edu.uno.advcomparch.statemachine.L1ControllerStateMachineConfiguration;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.LinkedList;
import java.util.Queue;

@Configuration
@Import(L1ControllerStateMachineConfiguration.class)
public class TestConfiguration {

    @Bean
    public DataRepository<String, String> l1DataRepository() {
        return Mockito.mock(DataRepository.class);
    }

    @Bean
    public DataRepository<String, String> l2DataRepository() {
        return Mockito.mock(DataRepository.class);
    }

    @Bean
    public Level1Controller level1Controller() {
        return Mockito.mock(Level1Controller.class, Mockito.RETURNS_DEEP_STUBS);
    }

    @Bean
    public Level2Controller level2Controller() {
        return new Level2Controller(new LinkedList<>());
    }

    @Bean
    public CentralProcessingUnit cpu(Queue<Message> queue) {
        return Mockito.mock(DefaultCPU.class, Mockito.RETURNS_DEEP_STUBS);
    }

    @Bean
    // TODO - Delete
    public Queue<Message> MessageQueue() {
        return new LinkedList<>();
    }
}
