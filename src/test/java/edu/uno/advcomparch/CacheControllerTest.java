package edu.uno.advcomparch;

import edu.uno.advcomparch.config.ControllerConfiguration;
import edu.uno.advcomparch.cpu.DefaultCPU;
import edu.uno.advcomparch.statemachine.ControllerMessage;
import edu.uno.advcomparch.statemachine.ControllerState;
import edu.uno.advcomparch.statemachine.StateMachineMessageBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = ControllerConfiguration.class)
public class CacheControllerTest extends AbstractCompArchTest {

    private CacheController  cacheController;

    @Autowired
    public StateMachine<ControllerState, ControllerMessage> l1ControllerStateMachine;

    @Autowired
    public StateMachine<ControllerState, ControllerMessage> l2ControllerStateMachine;

    @Autowired
    public StateMachine<ControllerState, ControllerMessage> dramStateMachine;

    @Autowired
    public StateMachineMessageBus stateMachineMessageBus;

    @Autowired
    public static DefaultCPU cpu;

    @BeforeEach
    public void setUp() {
        cacheController = new CacheController(l1ControllerStateMachine,
                l2ControllerStateMachine,
                dramStateMachine,
                stateMachineMessageBus,
                cpu);
    }

    @Test
    public void testContext() {};

    @Test
    public void test() throws Exception {
        cacheController.runCacheOnFile("./src/test/resources/MessagesTest.txt");
    }

}
