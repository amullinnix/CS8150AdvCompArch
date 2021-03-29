package edu.uno.advcomparch.config;

import edu.uno.advcomparch.cpu.DefaultCPU;
import edu.uno.advcomparch.statemachine.L1ControllerStateMachineConfiguration;
import edu.uno.advcomparch.statemachine.StateMachineMessageBus;
import edu.uno.advcomparch.storage.Level1DataStore;
import edu.uno.advcomparch.storage.VictimCache;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import(L1ControllerStateMachineConfiguration.class)
@Configuration
public class L1StateMachineTestConfiguration {

    @Bean
    public Level1DataStore level1DataStore() {
        return Mockito.mock(Level1DataStore.class);
    }

    @Bean
    public DefaultCPU cpu() {
        return Mockito.mock(DefaultCPU.class, Mockito.RETURNS_DEEP_STUBS);
    }

    @Bean
    public VictimCache l1VictimCache() {
        return Mockito.mock(VictimCache.class);
    }

    @Bean
    public StateMachineMessageBus messageBus() {
        return Mockito.mock(StateMachineMessageBus.class);
    }

}
