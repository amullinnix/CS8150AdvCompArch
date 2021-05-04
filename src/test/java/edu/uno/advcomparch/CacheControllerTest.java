package edu.uno.advcomparch;

import edu.uno.advcomparch.config.ControllerConfiguration;
import edu.uno.advcomparch.controller.Address;
import edu.uno.advcomparch.cpu.DefaultCPU;
import edu.uno.advcomparch.statemachine.ControllerMessage;
import edu.uno.advcomparch.statemachine.ControllerState;
import edu.uno.advcomparch.statemachine.StateMachineMessageBus;
import edu.uno.advcomparch.storage.Level1DataStore;
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

    @Autowired
    Level1DataStore level1DataStore;

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
    public void ReadScenarioA() throws Exception {
        outputLogger.info("Starting Read Test");
        cacheController.runCacheOnFile("./src/test/resources/edu/uno/advcomparch/cache_tests/ScenarioAReadTest.txt");

        Thread.sleep(20000);

    }

    @Test
    public void ReadScenarioAWithPrepopulatedL1Cache() throws Exception {
        byte b = 1;
        level1DataStore.writeDataToCache(new Address("101010", "101010", "10101"), b);

        outputLogger.info("Starting pre-populated Read Test");
        cacheController.runCacheOnFile("./src/test/resources/edu/uno/advcomparch/cache_tests/ScenarioAReadTest.txt");

        Thread.sleep(20000);

    }

    @Test
    public void WriteScenarioB() throws Exception {
        outputLogger.info("Starting Write Test");
        cacheController.runCacheOnFile("./src/test/resources/edu/uno/advcomparch/cache_tests/ScenarioBWriteTest.txt");

        Thread.sleep(20000);

    }

    @Test
    public void WriteScenarioBWithPrepopulatedL1Cache() throws Exception {
        byte b = 1;
        level1DataStore.writeDataToCache(new Address("101010", "101010", "10101"), b);

        outputLogger.info("Starting pre-populated Write Test");
        cacheController.runCacheOnFile("./src/test/resources/edu/uno/advcomparch/cache_tests/ScenarioBWriteTest.txt");

        Thread.sleep(20000);

    }

    @Test
    public void ReadWriteSameScenarioC() throws Exception {
        outputLogger.info("Starting Read Write Same Test");
        cacheController.runCacheOnFile("./src/test/resources/edu/uno/advcomparch/cache_tests/ScenarioCBackBackReadWriteSame.txt");

        Thread.sleep(20000);

    }

    @Test
    public void ReadWriteDiffScenarioD() throws Exception {
        outputLogger.info("Starting Read Write Diff Test");
        cacheController.runCacheOnFile("./src/test/resources/edu/uno/advcomparch/cache_tests/ScenarioDBackBackReadWriteDiff.txt");

        Thread.sleep(20000);
    }

    @Test
    public void ReadFromL1VictimCacheScenarioE() throws Exception {
        outputLogger.info("Starting Victim Cache Test");
        cacheController.runCacheOnFile("./src/test/resources/edu/uno/advcomparch/cache_tests/ScenarioEReadWithVictimCache.txt");

        Thread.sleep(20000);

    }

    @Test
    public void ReadL1WBL2WBScenarioF() throws Exception {
        outputLogger.info("Starting Read with L1 L2 Write Buffer Test");
        cacheController.runCacheOnFile("./src/test/resources/edu/uno/advcomparch/cache_tests/ScenarioFReadWithL1L2WriteBuffer.txt");

        Thread.sleep(20000);

    }

    @Test
    public void MutualExclusionViolationSenarioG() throws Exception {
        // TODO - FIXME

        Thread.sleep(20000);

    }
}
