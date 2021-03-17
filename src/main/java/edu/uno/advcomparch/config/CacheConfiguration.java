package edu.uno.advcomparch.config;

import edu.uno.advcomparch.cpu.CentralProcessingUnit;
import edu.uno.advcomparch.cpu.DefaultCPU;
import edu.uno.advcomparch.instruction.Message;
import edu.uno.advcomparch.repository.DataRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedList;
import java.util.Queue;

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
    public CentralProcessingUnit cpu(Queue<Message> queue) {
        return new DefaultCPU(queue);
    }

    @Bean
    // TODO - Delete
    public Queue<Message> MessageQueue() {
        return new LinkedList<>();
    }
}
