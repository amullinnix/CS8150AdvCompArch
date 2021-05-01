package edu.uno.advcomparch;

import edu.uno.advcomparch.config.ControllerConfiguration;
import edu.uno.advcomparch.cpu.DefaultCPU;
import edu.uno.advcomparch.statemachine.ControllerMessage;
import edu.uno.advcomparch.statemachine.ControllerState;
import edu.uno.advcomparch.statemachine.StateMachineMessageBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.test.context.ContextConfiguration;

import java.util.LinkedList;

@ContextConfiguration(classes = ControllerConfiguration.class)
public class CacheControllerTest extends AbstractCompArchTest {

    private final Logger outputLogger = LoggerFactory.getLogger("output");

    private CacheController  cacheController;

    @Autowired
    public StateMachine<ControllerState, ControllerMessage> l1ControllerStateMachine;

    @Autowired
    public StateMachine<ControllerState, ControllerMessage> l2ControllerStateMachine;

    @Autowired
    public StateMachine<ControllerState, ControllerMessage> dramStateMachine;

    @Autowired
    public StateMachineMessageBus stateMachineMessageBus;

    public static DefaultCPU cpu;

    @BeforeEach
    public void setUp() {
        cpu = new DefaultCPU(new LinkedList<>());
        cacheController = new CacheController(l1ControllerStateMachine,
                l2ControllerStateMachine,
                dramStateMachine,
                stateMachineMessageBus,
                cpu);
    }

    @Test
    public void testContext() {};

    @Test
    public void testRead() throws Exception {
        outputLogger.info("Starting Read Test");
        cacheController.runCacheOnFile("./src/test/resources/edu/uno/advcomparch/cache_tests/ScenarioAReadTest.txt");
    }

    @Test
    public void test() throws Exception {
        outputLogger.info("Starting Write Test");
        cacheController.runCacheOnFile("./src/test/resources/edu/uno/advcomparch/cache_tests/ScenarioBWriteTest.txt");
    }

}
