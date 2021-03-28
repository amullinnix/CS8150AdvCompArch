package edu.uno.advcomparch.config;

import edu.uno.advcomparch.storage.VictimCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SimpleTestConfiguration {

    @Bean
    public VictimCache l1VictimCache() {
        return new VictimCache();
    }
}
