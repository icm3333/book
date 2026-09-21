package rcm.book.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import rcm.book.model.ReadingStatus;

/** Request body used to change the reading status of a tracked book. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateStatusRequestDTO {

    @NotNull(message = "Reading status cannot be null")
    private ReadingStatus status;
}
