package edu.uno.advcomparch.config;

import edu.uno.advcomparch.cpu.CentralProcessingUnit;
import edu.uno.advcomparch.cpu.DefaultCPU;
import edu.uno.advcomparch.repository.DataRepository;
import edu.uno.advcomparch.storage.VictimCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedList;

@Configuration
public class CacheConfiguration {

    @Bean
    public DataRepository<String, String> l1DataRepository() {
        return new DataRepository<>();
    }

    @Bean
    public DataRepository<String, String> l2DataRepository() {
        return new DataRepository<>();
    }

    @Bean
    public CentralProcessingUnit cpu() {
        return new DefaultCPU(new LinkedList<>());
    }

    @Bean
    public VictimCache l1VictimCache() {
        return new VictimCache();
    }
}
