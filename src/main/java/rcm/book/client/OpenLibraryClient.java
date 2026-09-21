package rcm.book.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import rcm.book.dto.OpenLibraryResponseDTO;
import rcm.book.exception.ExternalServiceException;

/**
 * Read-only access to the Open Library search API.
 *
 * <p>Upstream problems are translated into {@link ExternalServiceException} so a third-party
 * outage surfaces as a deliberate 502 instead of an unhandled 500.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OpenLibraryClient {

    /** Open Library returns 100 documents by default; a bounded page keeps responses small. */
    private static final int MAX_RESULTS = 20;

    private final RestClient openLibraryRestClient;

    public OpenLibraryResponseDTO searchBooks(String query){
        try {
            return openLibraryRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search.json")
                            .queryParam("q", query)
                            .queryParam("limit", MAX_RESULTS)
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new ExternalServiceException(
                                "Open Library responded with status " + response.getStatusCode() + ".");
                    })
                    .body(OpenLibraryResponseDTO.class);

        } catch (ResourceAccessException ex) {
            // Connection refused, DNS failure or timeout.
            log.warn("Open Library is unreachable: {}", ex.getMessage());
            throw new ExternalServiceException("Open Library is currently unreachable.", ex);
        }
    }
}
