package rcm.book;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Confirms the OpenAPI description is generated, reachable without a token, and advertises the
 * JWT bearer scheme.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://localhost:5432/book}")
class OpenApiDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void apiDocs_ShouldBePubliclyAvailable() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Book Tracker API"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));
    }

    @Test
    void apiDocs_ShouldDocumentTheLibraryAndAuthEndpoints() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/auth/login']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/books/me']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/books/me/stats']").exists());
    }
}
