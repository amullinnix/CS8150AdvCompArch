package edu.uno.advcomparch.utility;

import edu.uno.advcomparch.instruction.Instruction;
import edu.uno.advcomparch.instruction.Message;
import edu.uno.advcomparch.model.Controller;
import org.junit.Before;
import org.junit.Test;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import static edu.uno.advcomparch.model.Controller.CPU;
import static edu.uno.advcomparch.model.Controller.LEVEL_1_CACHE;
import static org.junit.Assert.assertEquals;


public class RouterTest {

    Queue<Message> level1Queue;
    Queue<Message> cpuQueue;

    Message message;

    @Before
    public void setup() {
        level1Queue = new LinkedBlockingQueue<>();
        cpuQueue = new LinkedBlockingQueue<>();
        message = new Message();
    }

    @Test
    public void routeMessageToLevel1() {

        //Inject two queues into router
        Router router = new Router(level1Queue, cpuQueue);

        message.setSource(CPU);
        message.setDestination(LEVEL_1_CACHE);

        router.route(message);

        assertEquals(1, level1Queue.size());
    }

    @Test
    public void routeMessageToCpu() {
        Router router = new Router(level1Queue, cpuQueue);

        message.setSource(LEVEL_1_CACHE);
        message.setDestination(CPU);

        router.route(message);

        assertEquals(1, cpuQueue.size());
    }

}