package edu.uno.advcomparch.config;

import edu.uno.advcomparch.cpu.DefaultCPU;
import edu.uno.advcomparch.repository.DataRepository;
import edu.uno.advcomparch.statemachine.L1ControllerState;
import edu.uno.advcomparch.statemachine.L1ControllerStateMachineConfiguration;
import edu.uno.advcomparch.statemachine.StateMachineMessageBus;
import edu.uno.advcomparch.storage.Level1DataStore;
import edu.uno.advcomparch.storage.VictimCache;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.statemachine.StateMachine;

@Import(L1ControllerStateMachineConfiguration.class)
@Configuration
public class L1StateMachineTestConfiguration {

    @Bean
    public DataRepository<String, String> l1DataRepository() {
        return Mockito.mock(DataRepository.class);
    }

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
    public StateMachine<L1ControllerState, L1ControllerState> l2ControllerStateMachine() {
        return Mockito.mock(StateMachine.class);
    }

    @Bean
    public StateMachineMessageBus messageBus() {
        return Mockito.mock(StateMachineMessageBus.class);
    }

}
