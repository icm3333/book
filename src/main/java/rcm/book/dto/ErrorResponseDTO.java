package rcm.book.dto;

import java.time.LocalDateTime;

/**
 * Standard error payload returned by every failing endpoint.
 *
 * <p>Keeping a single shape for all errors means clients only ever need to parse one
 * structure, regardless of whether the failure came from validation, business rules
 * or the framework itself.
 */
public record ErrorResponseDTO(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
