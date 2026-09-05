package rcm.book.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserStatsResponseDTO {
    private Long userId;
    private long totalBooks;
    private long completedBooks;
    private long readingBooks;
    private long toReadBooks;
    private int totalPagesRead;
}
