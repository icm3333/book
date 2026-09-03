package rcm.book.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class OpenLibraryBookDTO {
    @JsonProperty("key")
    private String key;

    @JsonProperty("title")
    private String title;

    @JsonProperty("author_name")
    private List<String> author;

    @JsonProperty("cover_i")
    private Integer coverId;

    @JsonProperty("number_of_pages_median")
    private Integer numberOfPagesMedian;

}
