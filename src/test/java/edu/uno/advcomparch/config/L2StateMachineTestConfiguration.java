package edu.uno.advcomparch.config;

import edu.uno.advcomparch.statemachine.L2ControllerStateMachineConfiguration;
import edu.uno.advcomparch.statemachine.StateMachineMessageBus;
import edu.uno.advcomparch.storage.DynamicRandomAccessMemory;
import edu.uno.advcomparch.storage.Level1WriteBuffer;
import edu.uno.advcomparch.storage.Level2DataStore;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(L2ControllerStateMachineConfiguration.class)
public class L2StateMachineTestConfiguration {

    @Bean
    public Level2DataStore level2DataStore() {
        return Mockito.mock(Level2DataStore.class);
    }

    @Bean
    public DynamicRandomAccessMemory dynamicRandomAccessMemory() {
        return Mockito.mock(DynamicRandomAccessMemory.class);
    }

    @Bean
    public StateMachineMessageBus messageBus() {
        return Mockito.mock(StateMachineMessageBus.class);
    }

    @Bean
    public Level1WriteBuffer level1WriteBuffer() {
        return Mockito.mock(Level1WriteBuffer.class);
    }
}
