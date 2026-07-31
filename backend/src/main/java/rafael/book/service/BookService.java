package rafael.book.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rafael.book.client.OpenLibraryClient;
import rafael.book.controller.BookController;
import rafael.book.model.Book;
import rafael.book.repository.BookRepository;

import java.util.List;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private OpenLibraryClient openLibraryClient;

    public List<Book> listAll(){ return bookRepository.findAll(); }

    public Book registerByTitle(String titleSearch, Integer currentPage, String status){
        OpenLibraryClient.ExternalBookDTO externalData = openLibraryClient.searchByTitle(titleSearch);

        Book book = new Book();
        if(externalData != null){
            book.setTitle(externalData.getTitle());
            book.setAuthor(externalData.getAuthor());
            book.setTotalPages(externalData.getTotalPages());
        }else{
            book.setTitle(titleSearch);
            book.setAuthor("Unknown");
            book.setTotalPages(0);
        }

        book.setCurrentPage(currentPage != null ? currentPage : 0);
        book.setStatus(status != null ? status : "TO_READ");

        return bookRepository.save(book);
    }
    public void delete(Long id){
        bookRepository.deleteById(id);
    }

    public void updatePartial(Long id, BookController.BookRequestDTO request){
        Book book = bookRepository.findById(id).orElseThrow(() -> new RuntimeException("The id " + id + " is not registred on db"));

        if(request.getStatus() != null && !request.getStatus().isBlank()){ book.setStatus(request.getStatus()); }
        if(request.getPagesRead() != null){ book.setCurrentPage(request.getPagesRead()); }

        bookRepository.save(book);
    }

}
