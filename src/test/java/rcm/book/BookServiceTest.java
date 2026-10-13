package rcm.book;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rcm.book.dto.TrackBookRequestDTO;
import rcm.book.exception.ResourceNotFoundException;
import rcm.book.model.Book;
import rcm.book.model.ReadingStatus;
import rcm.book.model.User;
import rcm.book.model.UserBook;
import rcm.book.repository.BookRepository;
import rcm.book.repository.UserBookRepository;
import rcm.book.repository.UserRepository;
import rcm.book.service.BookService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    private static final long USER_ID = 1L;
    private static final long OTHER_USER_ID = 2L;
    private static final long USER_BOOK_ID = 1L;

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
        testUser = new User(USER_ID, "iris", "iris@email.com", "senha123", null);
        testBook = new Book(1L, "OL2847540M", "SICP", "Harold Abelson", "bigurl", 420);
        testUserBook = new UserBook(USER_BOOK_ID, testUser, testBook, ReadingStatus.READING, 67, null, null);
    }

    @Test
    void updatePageProgress_ShouldUpdateSuccessfully_WhenPageIsValid(){
        when(userBookRepository.findByIdAndUserId(USER_BOOK_ID, USER_ID)).thenReturn(Optional.of(testUserBook));
        when(userBookRepository.save(any(UserBook.class))).thenReturn(testUserBook);

        var result = bookService.updatePageProgress(USER_ID, USER_BOOK_ID, 200);

        assertNotNull(result);
        assertEquals(200, result.getCurrentPage());
        verify(userBookRepository, times(1)).save(testUserBook);
    }

    @Test
    void updatePageProgress_ShouldThrowException_WhenPageExceedsTotal(){
        when(userBookRepository.findByIdAndUserId(USER_BOOK_ID, USER_ID)).thenReturn(Optional.of(testUserBook));

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> bookService.updatePageProgress(USER_ID, USER_BOOK_ID, 500));

        assertTrue(exception.getMessage().contains("Current page cannot be greater than total book pages"));
        verify(userBookRepository, never()).save(any(UserBook.class));
    }

    @Test
    void updatePageProgress_ShouldThrowResourceNotFound_WhenUserBookDoesNotExist(){
        when(userBookRepository.findByIdAndUserId(99L, USER_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bookService.updatePageProgress(USER_ID, 99L, 424));
    }

    @Test
    void updatePageProgress_ShouldThrowResourceNotFound_WhenRecordBelongsToAnotherUser(){
        // The ownership-scoped query returns nothing for a record owned by somebody else.
        when(userBookRepository.findByIdAndUserId(USER_BOOK_ID, OTHER_USER_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bookService.updatePageProgress(OTHER_USER_ID, USER_BOOK_ID, 100));

        verify(userBookRepository, never()).save(any(UserBook.class));
    }

    @Test
    void updateReadingStatus_ShouldThrowResourceNotFound_WhenRecordBelongsToAnotherUser(){
        when(userBookRepository.findByIdAndUserId(USER_BOOK_ID, OTHER_USER_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bookService.updateReadingStatus(OTHER_USER_ID, USER_BOOK_ID, ReadingStatus.COMPLETED));

        verify(userBookRepository, never()).save(any(UserBook.class));
    }

    @Test
    void getUserStats_ShouldCalculateCorrectly_WhenUserHasBooks(){
        Book book1 = new Book(1L, "OL1", "Book1", "autor", "url", 200);
        Book book2 = new Book(2L, "ol2", "book2", "author", "url", 69);

        UserBook completed = new UserBook(1L, testUser, book1, ReadingStatus.COMPLETED, 200, null, null);
        UserBook reading = new UserBook(2L, testUser, book2, ReadingStatus.READING, 67, null, null);

        when(userBookRepository.findByUserId(USER_ID)).thenReturn(List.of(completed, reading));

        var stats = bookService.getUserStats(USER_ID);

        assertNotNull(stats);
        assertEquals(2, stats.getTotalBooks());
        assertEquals(1, stats.getCompletedBooks());
        assertEquals(1, stats.getReadingBooks());
        // 200 pages from the completed book plus 67 read so far in the second one.
        assertEquals(267, stats.getTotalPagesRead());
    }

    @Test
    void addBookToUserList_ShouldThrowResourceNotFound_WhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        TrackBookRequestDTO request = TrackBookRequestDTO.builder()
                .openLibraryId("OL123")
                .title("Title")
                .author("Author")
                .pageCount(100)
                .build();

        assertThrows(ResourceNotFoundException.class, () -> bookService.addBookToUserList(99L, request));
    }

    @Test
    void updateReadingStatus_ShouldUpdateSuccessfully_WhenValidStatusProvided() {
        when(userBookRepository.findByIdAndUserId(USER_BOOK_ID, USER_ID)).thenReturn(Optional.of(testUserBook));
        when(userBookRepository.save(any(UserBook.class))).thenReturn(testUserBook);

        var result = bookService.updateReadingStatus(USER_ID, USER_BOOK_ID, ReadingStatus.COMPLETED);

        assertNotNull(result);
        assertEquals(ReadingStatus.COMPLETED, result.getStatus());
        verify(userBookRepository, times(1)).save(testUserBook);
    }

    @Test
    void getUserBooks_ShouldReturnAllBooks_WhenStatusIsNull() {
        when(userBookRepository.findByUserId(USER_ID)).thenReturn(List.of(testUserBook));

        var books = bookService.getUserBooks(USER_ID, null);

        assertNotNull(books);
        assertEquals(1, books.size());
        verify(userBookRepository, times(1)).findByUserId(USER_ID);
    }

    @Test
    void getUserBooks_ShouldReturnFilteredBooks_WhenStatusIsProvided() {
        when(userBookRepository.findByUserIdAndStatus(USER_ID, ReadingStatus.READING))
                .thenReturn(List.of(testUserBook));

        var books = bookService.getUserBooks(USER_ID, ReadingStatus.READING);

        assertNotNull(books);
        assertEquals(1, books.size());
        verify(userBookRepository, times(1)).findByUserIdAndStatus(USER_ID, ReadingStatus.READING);
    }
}
