package rcm.book.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import rcm.book.dto.BookSearchResultDTO;
import rcm.book.dto.TrackBookRequestDTO;
import rcm.book.dto.UpdateProgressRequestDTO;
import rcm.book.dto.UpdateStatusRequestDTO;
import rcm.book.dto.UserBookResponseDTO;
import rcm.book.dto.UserStatsResponseDTO;
import rcm.book.model.ReadingStatus;
import rcm.book.model.User;
import rcm.book.service.BookService;

import java.util.List;

/**
 * Book catalog search and the authenticated user's reading library.
 *
 * <p>The library endpoints are all scoped to {@code /me} and resolve the user from the
 * validated JWT. No endpoint accepts a user id from the client, which is what prevents one
 * account from reading or modifying another account's data.
 */
@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Validated
public class BookController {

    private final BookService bookService;

    @GetMapping("/search")
    public ResponseEntity<List<BookSearchResultDTO>> searchBooks(
            @RequestParam @NotBlank(message = "Query cannot be empty") String query){

        return ResponseEntity.ok(bookService.searchBooks(query));
    }

    @GetMapping("/me")
    public ResponseEntity<List<UserBookResponseDTO>> getMyBooks(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) ReadingStatus status){

        return ResponseEntity.ok(bookService.getUserBooks(currentUser.getId(), status));
    }

    @PostMapping("/me")
    @ResponseStatus(HttpStatus.CREATED)
    public UserBookResponseDTO trackBook(
            @AuthenticationPrincipal User currentUser,
            @RequestBody @Valid TrackBookRequestDTO request){

        return bookService.addBookToUserList(currentUser.getId(), request);
    }

    @GetMapping("/me/stats")
    public ResponseEntity<UserStatsResponseDTO> getMyStats(@AuthenticationPrincipal User currentUser){
        return ResponseEntity.ok(bookService.getUserStats(currentUser.getId()));
    }

    @PatchMapping("/me/{userBookId}/progress")
    public ResponseEntity<UserBookResponseDTO> updateProgress(
            @AuthenticationPrincipal User currentUser,
            @PathVariable @Positive(message = "Tracking id must be positive") Long userBookId,
            @RequestBody @Valid UpdateProgressRequestDTO request){

        return ResponseEntity.ok(
                bookService.updatePageProgress(currentUser.getId(), userBookId, request.getCurrentPage()));
    }

    @PatchMapping("/me/{userBookId}/status")
    public ResponseEntity<UserBookResponseDTO> updateStatus(
            @AuthenticationPrincipal User currentUser,
            @PathVariable @Positive(message = "Tracking id must be positive") Long userBookId,
            @RequestBody @Valid UpdateStatusRequestDTO request){

        return ResponseEntity.ok(
                bookService.updateReadingStatus(currentUser.getId(), userBookId, request.getStatus()));
    }
}
