package rafael.book.client;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class OpenLibraryClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public ExternalBookDTO searchByTitle(String title){
        String url = "https://openlibrary.org/search.json?title=" + title.replace(" ", "+");

        ExternalBookDTO dto = new ExternalBookDTO();

        try{
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if(response != null && response.containsKey("docs")){
                List<Map<String, Object>> docs = (List<Map<String, Object>>) response.get("docs");

                if(!docs.isEmpty()){
                    Map<String, Object> firstBook = docs.get(0);

                    dto.setTitle((String) firstBook.get("title"));

                    List<String> authors = (List<String>) firstBook.get("author_name");
                    if(authors != null && !authors.isEmpty()){
                        dto.setAuthor(authors.get(0));
                    }else{
                        dto.setAuthor("Unknown");
                    }

                    Object pagesObj = firstBook.get("number_of_pages_median");
                    if (pagesObj instanceof Number) {
                        dto.setTotalPages(((Number) pagesObj).intValue());
                    } else {
                        dto.setTotalPages(0);
                    }

                    return dto;
                }
            }
        }catch(Exception e){
            System.err.println("Theres an error while consulting Open Library: " + e.getMessage());
        }

        dto.setTitle(title);
        dto.setAuthor("Unknown");
        dto.setTotalPages(0);
        return dto;
    }

    @Data
    public static class ExternalBookDTO{
        private String title;
        private String author;
        private Integer totalPages;
    }
}