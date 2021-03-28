package edu.uno.advcomparch;

import edu.uno.advcomparch.config.CacheConfiguration;
import edu.uno.advcomparch.statemachine.L1ControllerStateMachineConfiguration;
import edu.uno.advcomparch.statemachine.L2ControllerStateMachineConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({
		CacheConfiguration.class,
		L1ControllerStateMachineConfiguration.class,
		L2ControllerStateMachineConfiguration.class
})
public class AdvCompArchApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdvCompArchApplication.class, args);
	}

}
