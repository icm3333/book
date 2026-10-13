package rcm.book.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/** Request body used to start tracking a book in the caller's library. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackBookRequestDTO {

    @NotBlank(message = "Open Library ID cannot be blank")
    private String openLibraryId;

    @NotBlank(message = "Book title cannot be blank")
    private String title;

    private String author;

    @Min(value = 0, message = "Page count cannot be negative")
    private Integer pageCount;
}
