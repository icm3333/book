package rcm.book.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/** Request body used to move the caller's reading progress to a given page. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProgressRequestDTO {

    @NotNull(message = "Current page cannot be null")
    @Min(value = 0, message = "Page cannot be negative")
    private Integer currentPage;
}
