package edu.uno.advcomparch.config;

import edu.uno.advcomparch.controller.Level1Controller;
import edu.uno.advcomparch.controller.Level2Controller;
import edu.uno.advcomparch.cpu.DefaultCPU;
import edu.uno.advcomparch.repository.DataRepository;
import edu.uno.advcomparch.statemachine.L1ControllerStateMachineConfiguration;
import edu.uno.advcomparch.storage.Level1DataStore;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.LinkedList;

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
    public Level1DataStore level1DataStore() {
        return Mockito.mock(Level1DataStore.class);
    }

    @Bean
    public DefaultCPU cpu() {
        return Mockito.mock(DefaultCPU.class, Mockito.RETURNS_DEEP_STUBS);
    }
}
