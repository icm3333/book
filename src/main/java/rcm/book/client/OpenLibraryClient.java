package rcm.book.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import rcm.book.dto.OpenLibraryResponseDTO;

@Component
public class OpenLibraryClient {
    private final RestClient restClient;

    public OpenLibraryClient(){
        this.restClient = RestClient.builder()
                .baseUrl("https://openlibrary.org")
                .defaultHeader("User-Agent", "Booktracker (rafaelcardosomachado2@gmail.com)")
                .build();
    }

    public OpenLibraryResponseDTO searchBooks(String query){
        return restClient.get()
                .uri("/search.json?q={query}", query)
                .retrieve()
                .body(OpenLibraryResponseDTO.class);
    }
}
