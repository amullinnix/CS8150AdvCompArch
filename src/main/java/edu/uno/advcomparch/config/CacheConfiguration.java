package edu.uno.advcomparch.config;

import edu.uno.advcomparch.cpu.DefaultCPU;
import edu.uno.advcomparch.statemachine.StateMachineMessageBus;
import edu.uno.advcomparch.storage.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedList;

@Configuration
public class CacheConfiguration {

    @Bean
    public DefaultCPU cpu() {
        return new DefaultCPU(new LinkedList<>());
    }

    @Bean
    public StateMachineMessageBus stateMachineMessageBus() {
        return new StateMachineMessageBus();
    }

    @Bean
    public VictimCache l1VictimCache() {
        return new VictimCache();
    }

    @Bean
    public Level1WriteBuffer level1WriteBuffer() {
        return new Level1WriteBuffer();
    }

    @Bean
    public Level1DataStore level1DataStore(VictimCache l1VictimCache, Level1WriteBuffer level1WriteBuffer) {
        return new Level1DataStore(l1VictimCache, level1WriteBuffer);
    }

    @Bean
    public Level2WriteBuffer level2WriteBuffer() {
        return new Level2WriteBuffer();
    }

    @Bean
    public Level2DataStore level2DataStore(Level2WriteBuffer writeBuffer) {
        return new Level2DataStore(writeBuffer);
    }

    @Bean
    public DynamicRandomAccessMemory dynamicRandomAccessMemory() {
        return new DynamicRandomAccessMemory();
    }
}
