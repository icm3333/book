package rcm.book.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookSearchResultDTO {
    private String openLibraryId;
    private String title;
    private String author;
    private String coverImgUrl;
    private Integer pageCount;
}
