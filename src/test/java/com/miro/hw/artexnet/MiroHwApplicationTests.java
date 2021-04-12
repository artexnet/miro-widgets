package com.miro.hw.artexnet;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

@SpringBootTest
class MiroHwApplicationTests {
	private ConcurrentNavigableMap<Long, Integer> widgetsSequence = new ConcurrentSkipListMap<>(Collections.reverseOrder());

	@Test
	void contextLoads() {
		widgetsSequence.put(2L, 2);
		widgetsSequence.put(3L, 3);
		widgetsSequence.put(-5L, -5);
		widgetsSequence.put(1L, 1);
		widgetsSequence.put(-1L, -1);

		System.out.println(widgetsSequence);
	}

}
