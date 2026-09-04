package rcm.book.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rcm.book.dto.BookSearchResultDTO;
import rcm.book.dto.OpenLibraryResponseDTO;
import rcm.book.dto.UserBookResponseDTO;
import rcm.book.model.ReadingStatus;
import rcm.book.model.UserBook;
import rcm.book.service.BookService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController{

    private final BookService bookService;


    @GetMapping("/search")
    public ResponseEntity<List<BookSearchResultDTO>> searchBooks(@RequestParam String query){
        List<BookSearchResultDTO> results = bookService.searchBooks(query);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/track")
    public ResponseEntity<UserBookResponseDTO> addBookToUserList(
                            @RequestParam Long userId,
                            @RequestParam String openLibraryId,
                            @RequestParam String title,
                            @RequestParam(required = false) String author,
                            @RequestParam(required = false) Integer pageCount){

        UserBookResponseDTO trackedBook = bookService.addBookToUserList(userId, openLibraryId, title, author, pageCount);
        return ResponseEntity.ok(trackedBook);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserBookResponseDTO>> getUserBooks(
                            @PathVariable Long userId,
                            @RequestParam(required = false)ReadingStatus status){

        List<UserBookResponseDTO> books = bookService.getUserBooks(userId, status);
        return ResponseEntity.ok(books);
    }

    @PatchMapping("/tracking/{userBookId}/progress")
    public ResponseEntity<UserBookResponseDTO> updateProgress(
                            @PathVariable Long userBookId,
                            @RequestParam Integer page){

        UserBookResponseDTO updated = bookService.updatePageProgress(userBookId, page);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/tracking/{userBookId}/status")
    public ResponseEntity<UserBookResponseDTO> updateStatus(
                            @PathVariable Long userBookId,
                            @RequestParam ReadingStatus status){

        UserBookResponseDTO updated = bookService.updateReadingStatus(userBookId, status);
        return ResponseEntity.ok(updated);
    }

    // TODO: Patch for updateReadingStatus
}
