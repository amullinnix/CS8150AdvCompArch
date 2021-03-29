package edu.uno.advcomparch.config;

import edu.uno.advcomparch.statemachine.L1ControllerStateMachineConfiguration;
import edu.uno.advcomparch.statemachine.L2ControllerStateMachineConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import({
        CacheConfiguration.class,
        L1ControllerStateMachineConfiguration.class,
        L2ControllerStateMachineConfiguration.class
})
@Configuration
public class ControllerConfiguration {
}
