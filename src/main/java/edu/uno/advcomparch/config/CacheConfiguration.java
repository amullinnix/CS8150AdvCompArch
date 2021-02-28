package edu.uno.advcomparch.config;

import edu.uno.advcomparch.repository.DataRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfiguration {

    @Bean
    public DataRepository<String, Integer> l1DataRepository() {
        return new DataRepository<>();
    }

    @Bean
    public DataRepository<String, Integer> l2DataRepository() {
        return new DataRepository<>();
    }
}
