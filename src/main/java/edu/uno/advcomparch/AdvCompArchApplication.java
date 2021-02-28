package edu.uno.advcomparch;

import edu.uno.advcomparch.config.CacheConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(CacheConfiguration.class)
public class AdvCompArchApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdvCompArchApplication.class, args);
	}

}
