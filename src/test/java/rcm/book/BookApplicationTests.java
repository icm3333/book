package rcm.book;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Verifies the full application context wires up.
 *
 * <p>The datasource is pinned here so the test does not pick up whatever {@code DB_URL} happens
 * to be exported in the developer's shell. Override with {@code TEST_DB_URL} when the local
 * test database lives elsewhere.
 */
@SpringBootTest
@TestPropertySource(properties = "spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/book}")
class BookApplicationTests {

	@Test
	void contextLoads() {
	}

}
