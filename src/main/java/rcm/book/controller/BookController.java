package rcm.book.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import rcm.book.dto.BookSearchResultDTO;
import rcm.book.dto.OpenLibraryResponseDTO;
import rcm.book.dto.UserBookResponseDTO;
import rcm.book.dto.UserStatsResponseDTO;
import rcm.book.model.ReadingStatus;
import rcm.book.model.UserBook;
import rcm.book.service.BookService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Validated
public class BookController{

    private final BookService bookService;


    @GetMapping("/search")
    public ResponseEntity<List<BookSearchResultDTO>> searchBooks(
                        @RequestParam @NotBlank(message = "Query cannot be empty") String query){

        List<BookSearchResultDTO> results = bookService.searchBooks(query);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/track")
    public ResponseEntity<UserBookResponseDTO> addBookToUserList(
                            @RequestParam @Positive(message = "userID must be positive") @NotNull(message = "User ID cannot be null") Long userId,
                            @RequestParam @NotBlank(message = "Open Library ID cannot be blank") String openLibraryId,
                            @RequestParam @NotBlank(message = "Book title cannot be blank") String title,
                            @RequestParam(required = false) String author,
                            @RequestParam(required = false) @Min(value=0, message = "Page cannot be negative") Integer pageCount){

        UserBookResponseDTO trackedBook = bookService.addBookToUserList(userId, openLibraryId, title, author, pageCount);
        return ResponseEntity.ok(trackedBook);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserBookResponseDTO>> getUserBooks(
                            @PathVariable @Positive(message = "userID must be positive") @NotNull(message = "User ID cannot be null") Long userId,
                            @RequestParam(required = false) ReadingStatus status){

        List<UserBookResponseDTO> books = bookService.getUserBooks(userId, status);
        return ResponseEntity.ok(books);
    }

    @PatchMapping("/tracking/{userBookId}/progress")
    public ResponseEntity<UserBookResponseDTO> updateProgress(
                            @PathVariable @Positive(message = "Userbook ID must be positive") Long userBookId,
                            @RequestParam @Min(value=0, message = "Page cannot be negative")  Integer page){

        UserBookResponseDTO updated = bookService.updatePageProgress(userBookId, page);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/tracking/{userBookId}/status")
    public ResponseEntity<UserBookResponseDTO> updateStatus(
                            @PathVariable @Positive(message = "Userbook ID must be positive") Long userBookId,
                            @RequestParam @NotNull(message = "Reading status cannot be null") ReadingStatus status){

        UserBookResponseDTO updated = bookService.updateReadingStatus(userBookId, status);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("user/{userId}/stats")
    public ResponseEntity<UserStatsResponseDTO> getUserStats(
            @PathVariable @Positive(message = "userID must be positive") @NotNull(message = "User ID cannot be null") Long userId){

        UserStatsResponseDTO stats = bookService.getUserStats(userId);
        return ResponseEntity.ok(stats);
    }
}
