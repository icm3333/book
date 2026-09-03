package rcm.book.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rcm.book.dto.OpenLibraryResponseDTO;
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
    public ResponseEntity<OpenLibraryResponseDTO> searchBooks(@RequestParam String query){
        OpenLibraryResponseDTO result = bookService.searchBooksAPI(query);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/track")
    public ResponseEntity<UserBook> addBookToUserList(
                            @RequestParam Long userId,
                            @RequestParam String openLibraryId,
                            @RequestParam String title,
                            @RequestParam(required = false) String author,
                            @RequestParam(required = false) Integer pageCount){

        UserBook trackedBook = bookService.addBookToUserList(userId, openLibraryId, title, author, pageCount);
        return ResponseEntity.ok(trackedBook);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserBook>> getUserBooks(
                            @PathVariable Long userId,
                            @RequestParam(required = false)ReadingStatus status){

        List<UserBook> books = bookService.getUserBooks(userId, status);
        return ResponseEntity.ok(books);
    }

    @PatchMapping("/tracking/{userBookId}/progress")
    public ResponseEntity<UserBook> updateProgress(
                            @PathVariable Long userBookId,
                            @RequestParam Integer page){

        UserBook updated = bookService.updatePageProgress(userBookId, page);
        return ResponseEntity.ok(updated);
    }
}
