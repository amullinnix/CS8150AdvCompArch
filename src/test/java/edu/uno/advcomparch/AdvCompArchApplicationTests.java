package edu.uno.advcomparch;

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

}