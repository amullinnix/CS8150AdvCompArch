package edu.uno.advcomparch.config;

import edu.uno.advcomparch.storage.Level1WriteBuffer;
import edu.uno.advcomparch.storage.Level2WriteBuffer;
import edu.uno.advcomparch.storage.VictimCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SimpleTestConfiguration {

    @Bean
    public VictimCache l1VictimCache() {
        return new VictimCache();
    }

    @Bean
    public Level1WriteBuffer writeBuffer() {
        return new Level1WriteBuffer();
    }

    @Bean
    public Level2WriteBuffer level2WriteBuffer() {
        return new Level2WriteBuffer();
    }

}
