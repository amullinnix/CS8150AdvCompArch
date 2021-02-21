package edu.uno.advcomparch;

import edu.uno.advcomparch.CacheController;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AdvCompArchApplicationTests {
	
	@Autowired
	private CacheController controller;

	@Test
	void contextLoads() {
		assertThat(controller).isNotNull();
	}

	@Test
	void proveTestCanFail() {
		assertThat(controller.stubMethod()).isTrue();
	}

	@Test
	void controllerHasQueue() {
		assertThat(controller.getQueue()).isNotNull();
	}

	@Test
	void controllerCanProcessMessage() {
		//controller's queue needs something to process
		controller.getQueue().add(new Instruction());

		//process a message
		controller.processMessage();

		//assert that the queue is now empty
		assertThat(controller.getQueue()).hasSize(0);
	}

}
