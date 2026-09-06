package rcm.book;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rcm.book.exception.ResourceNotFoundException;
import rcm.book.model.Book;
import rcm.book.model.ReadingStatus;
import rcm.book.model.UserBook;
import rcm.book.repository.BookRepository;
import rcm.book.repository.UserBookRepository;
import rcm.book.repository.UserRepository;
import rcm.book.service.BookService;
import rcm.book.model.User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserBookRepository userBookRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookService bookService;

    private User testUser;
    private Book testBook;
    private UserBook testUserBook;

    @BeforeEach
    void setUp(){
        testUser = new User(1L, "iris", "iris@email.com", "senha123");
        testBook = new Book(1L, "OL2847540M", "SICP", "Harold Abelson", "bigurl", 420);
        testUserBook = new UserBook(1L, testUser, testBook, ReadingStatus.READING, 67, null, null);
    }

    @Test
    void updatePageProgress_ShouldUpdateSuccessfully_WhenPageIsValid(){
        when(userBookRepository.findById(1L)).thenReturn(Optional.of(testUserBook));
        when(userBookRepository.save(any(UserBook.class))).thenReturn(testUserBook);

        var result = bookService.updatePageProgress(1L, 200);

        assertNotNull(result);
        assertEquals(200, result.getCurrentPage());
        verify(userBookRepository, times(1)).save(testUserBook);
    }

    @Test
    void updatePageProgress_ShouldThrowException_WhenPageExceedsTotal(){
        when(userBookRepository.findById(1L)).thenReturn(Optional.of(testUserBook));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bookService.updatePageProgress(1L, 500);
        });

        assertTrue(exception.getMessage().contains("Current page cannot be greater than total book pages"));
    }

    @Test
    void updatePageProgress_ShouldThrowResourceNotFound_WhenUserBookDoesNotExist(){
        when(userBookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            bookService.updatePageProgress(99L, 424);
        });
    }

    @Test
    void getUserStats_ShouldCalculateCorrectly_WhenUserHasBooks(){
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Book book1 = new Book(1L, "OL1", "Book1", "autor", "url", 200);
        Book book2 = new Book(2L, "ol2", "book2", "author", "url", 69);

        UserBook ub1 = new UserBook(1L, testUser, book1, ReadingStatus.COMPLETED, 200, null, null);
        UserBook ub2 = new UserBook(2L, testUser, book2, ReadingStatus.READING, 67, null, null);

        when(userBookRepository.findByUserId(1L)).thenReturn(List.of(ub1, ub2));
        var stats = bookService.getUserStats(1L);

        assertNotNull(stats);
        assertEquals(2, stats.getTotalBooks());
        assertEquals(1, stats.getCompletedBooks());
        assertEquals(1, stats.getReadingBooks());
        // total expected 267
        assertEquals(267, stats.getTotalPagesRead());
    }

    @Test
    void addBookToUserList_ShouldThrowResourceNotFound_WhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            bookService.addBookToUserList(99L, "OL123", "Title", "Author", 100);
        });
    }

    @Test
    void updateReadingStatus_ShouldUpdateSuccessfully_WhenValidStatusProvided() {
        when(userBookRepository.findById(1L)).thenReturn(Optional.of(testUserBook));
        when(userBookRepository.save(any(UserBook.class))).thenReturn(testUserBook);

        var result = bookService.updateReadingStatus(1L, ReadingStatus.COMPLETED);

        assertNotNull(result);
        assertEquals(ReadingStatus.COMPLETED, result.getStatus());
        verify(userBookRepository, times(1)).save(testUserBook);
    }

    @Test
    void getUserBooks_ShouldReturnAllBooks_WhenStatusIsNull() {
        when(userBookRepository.findByUserId(1L)).thenReturn(List.of(testUserBook));

        var books = bookService.getUserBooks(1L, null);

        assertNotNull(books);
        assertEquals(1, books.size());
        verify(userBookRepository, times(1)).findByUserId(1L);
    }

    @Test
    void getUserBooks_ShouldReturnFilteredBooks_WhenStatusIsProvided() {
        when(userBookRepository.findByUserIdAndStatus(1L, ReadingStatus.READING)).thenReturn(List.of(testUserBook));

        var books = bookService.getUserBooks(1L, ReadingStatus.READING);

        assertNotNull(books);
        assertEquals(1, books.size());
        verify(userBookRepository, times(1)).findByUserIdAndStatus(1L, ReadingStatus.READING);
    }
}
