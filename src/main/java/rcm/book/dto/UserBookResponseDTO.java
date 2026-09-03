package rcm.book.dto;

import lombok.Builder;
import lombok.Data;
import rcm.book.model.ReadingStatus;

import java.time.LocalDate;

@Data
@Builder
public class UserBookResponseDTO {

    private Long id;
    private String title;
    private String author;
    private String openLibraryId;
    private String coverImgURL;
    private Integer pageCount;
    private ReadingStatus status;
    private Integer currentPage;
    private LocalDate startDate;
    private LocalDate finishDate;
}
