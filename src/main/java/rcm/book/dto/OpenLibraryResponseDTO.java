package rcm.book.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class OpenLibraryResponseDTO {
    @JsonProperty("docs")
    private List<OpenLibraryBookDTO> docs;
}
