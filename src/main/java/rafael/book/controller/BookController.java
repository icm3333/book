package rafael.book.controller;


import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import rafael.book.model.Book;
import rafael.book.service.BookService;

import java.util.List;

@RestController
@RequestMapping("/v1/api/books")
public class BookController {
    @Autowired
    private BookService bookService;

    @GetMapping
    public List<Book> list(){
        return bookService.listAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Book save(@RequestBody BookRequestDTO request){
        return bookService.registerByTitle(request.getTitleSearch(), request.getPagesRead(), request.getStatus());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){
        bookService.delete(id);
    }

    @Data
    public static class BookRequestDTO{
        private String titleSearch;
        private Integer pagesRead;
        private String status;
    }
}
