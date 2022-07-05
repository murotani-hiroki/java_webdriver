package com.mrtn.scraping;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

//@SpringBootTest
class ScrapingApplicationTests {

	//@Test
	void contextLoads() {
	}

	@Test
	void debug() {
		String results =  String.join(",", new String[]{"a",null,"c"});
		assertThat(results).isEqualTo("a,b,c");
	}
}
