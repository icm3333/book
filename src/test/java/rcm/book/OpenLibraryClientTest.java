package rcm.book;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import rcm.book.client.OpenLibraryClient;
import rcm.book.dto.OpenLibraryResponseDTO;
import rcm.book.exception.ExternalServiceException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.hamcrest.Matchers.containsString;

class OpenLibraryClientTest {

    private MockRestServiceServer server;
    private OpenLibraryClient client;

    @BeforeEach
    void setUp(){
        RestClient.Builder builder = RestClient.builder().baseUrl("https://openlibrary.org");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new OpenLibraryClient(builder.build());
    }

    @Test
    void searchBooks_ShouldMapResponse_WhenUpstreamSucceeds(){
        String json = """
                {
                  "docs": [
                    {
                      "key": "/works/OL1W",
                      "title": "Structure and Interpretation of Computer Programs",
                      "author_name": ["Harold Abelson"],
                      "cover_i": 12345,
                      "number_of_pages_median": 657
                    }
                  ]
                }
                """;

        server.expect(requestTo(containsString("/search.json")))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        OpenLibraryResponseDTO response = client.searchBooks("sicp");

        assertNotNull(response);
        assertEquals(1, response.getDocs().size());
        assertEquals("/works/OL1W", response.getDocs().get(0).getKey());
        assertEquals(12345, response.getDocs().get(0).getCoverId());
        server.verify();
    }

    @Test
    void searchBooks_ShouldBoundResults_ByRequestingALimit(){
        server.expect(requestTo(containsString("limit=20")))
                .andRespond(withSuccess("{\"docs\":[]}", MediaType.APPLICATION_JSON));

        client.searchBooks("sicp");

        server.verify();
    }

    @Test
    void searchBooks_ShouldThrowExternalServiceException_WhenUpstreamFails(){
        server.expect(requestTo(containsString("/search.json")))
                .andRespond(withServerError());

        ExternalServiceException exception =
                assertThrows(ExternalServiceException.class, () -> client.searchBooks("sicp"));

        assertTrue(exception.getMessage().contains("Open Library responded with status"));
        server.verify();
    }

    @Test
    void searchBooks_ShouldThrowExternalServiceException_WhenUpstreamReturnsNotFound(){
        server.expect(requestTo(containsString("/search.json")))
                .andRespond(org.springframework.test.web.client.response.MockRestResponseCreators
                        .withStatus(HttpStatus.NOT_FOUND));

        assertThrows(ExternalServiceException.class, () -> client.searchBooks("sicp"));
        server.verify();
    }
}
