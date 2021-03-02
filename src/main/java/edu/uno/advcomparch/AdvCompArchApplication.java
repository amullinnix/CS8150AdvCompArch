package edu.uno.advcomparch;

import edu.uno.advcomparch.config.CacheConfiguration;
import edu.uno.advcomparch.config.SimpleStateMachineConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({
		CacheConfiguration.class,
		SimpleStateMachineConfiguration.class
})
public class AdvCompArchApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdvCompArchApplication.class, args);
	}

}
