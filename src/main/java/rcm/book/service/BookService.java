package rcm.book.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rcm.book.client.OpenLibraryClient;
import rcm.book.dto.BookSearchResultDTO;
import rcm.book.dto.OpenLibraryResponseDTO;
import rcm.book.dto.TrackBookRequestDTO;
import rcm.book.dto.UserBookResponseDTO;
import rcm.book.dto.UserStatsResponseDTO;
import rcm.book.exception.DuplicateResourceException;
import rcm.book.exception.ResourceNotFoundException;
import rcm.book.model.Book;
import rcm.book.model.ReadingStatus;
import rcm.book.model.User;
import rcm.book.model.UserBook;
import rcm.book.repository.BookRepository;
import rcm.book.repository.UserBookRepository;
import rcm.book.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Reading-library operations.
 *
 * <p>Every user-scoped method takes the owning {@code userId} as its first argument and
 * resolves tracking records through an ownership-scoped query. Callers must supply the id
 * of the authenticated user; it is never accepted from client input.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private static final String COVER_URL_TEMPLATE = "https://covers.openlibrary.org/b/id/%d-M.jpg";
    private static final String UNKNOWN_AUTHOR = "Unknown";

    private final BookRepository bookRepository;
    private final UserBookRepository userBookRepository;
    private final UserRepository userRepository;
    private final OpenLibraryClient openLibraryClient;

    public List<BookSearchResultDTO> searchBooks(String query){
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query is empty.");
        }

        OpenLibraryResponseDTO response = openLibraryClient.searchBooks(query);
        if (response == null || response.getDocs() == null) {
            return List.of();
        }

        return response.getDocs().stream().map(doc -> BookSearchResultDTO.builder()
                .openLibraryId(doc.getKey())
                .title(doc.getTitle())
                .author(firstAuthorOf(doc.getAuthor()))
                .coverImgUrl(coverUrlOf(doc.getCoverId()))
                .pageCount(doc.getNumberOfPagesMedian())
                .build()).toList();
    }

    @Transactional
    public UserBookResponseDTO addBookToUserList(Long userId, TrackBookRequestDTO request){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Book book = bookRepository.findByOpenLibraryId(request.getOpenLibraryId())
                .orElseGet(() -> bookRepository.save(newBookFrom(request)));

        if (userBookRepository.existsByUserIdAndBookId(userId, book.getId())) {
            throw new DuplicateResourceException("Book is already in this user's list.");
        }

        UserBook userBook = new UserBook();
        userBook.setUser(user);
        userBook.setBook(book);
        userBook.setStatus(ReadingStatus.TO_READ);
        userBook.setCurrentPage(0);

        return toResponse(userBookRepository.save(userBook));
    }

    public List<UserBookResponseDTO> getUserBooks(Long userId, ReadingStatus status){
        List<UserBook> userBooks = (status != null)
                ? userBookRepository.findByUserIdAndStatus(userId, status)
                : userBookRepository.findByUserId(userId);

        return userBooks.stream().map(this::toResponse).toList();
    }

    @Transactional
    public UserBookResponseDTO updateReadingStatus(Long userId, Long userBookId, ReadingStatus newStatus){
        UserBook userBook = requireOwnedUserBook(userId, userBookId);
        userBook.setStatus(newStatus);

        if (newStatus == ReadingStatus.READING && userBook.getStartDate() == null) {
            userBook.setStartDate(LocalDate.now());
        } else if (newStatus == ReadingStatus.COMPLETED) {
            // Completing a book implies it was started, and that every page was read.
            if (userBook.getStartDate() == null) {
                userBook.setStartDate(LocalDate.now());
            }
            userBook.setFinishDate(LocalDate.now());
            if (userBook.getBook().getPageCount() != null) {
                userBook.setCurrentPage(userBook.getBook().getPageCount());
            }
        }

        return toResponse(userBookRepository.save(userBook));
    }

    @Transactional
    public UserBookResponseDTO updatePageProgress(Long userId, Long userBookId, Integer newPage){
        UserBook userBook = requireOwnedUserBook(userId, userBookId);

        if (newPage == null || newPage < 0) {
            throw new IllegalArgumentException("Page cannot be negative.");
        }

        Integer totalPages = userBook.getBook().getPageCount();
        if (totalPages != null && newPage > totalPages) {
            throw new IllegalArgumentException(
                    "Current page cannot be greater than total book pages (" + totalPages + ").");
        }
        userBook.setCurrentPage(newPage);

        // Recording progress on an untouched book implicitly starts it.
        if (newPage > 0 && userBook.getStatus() == ReadingStatus.TO_READ) {
            userBook.setStatus(ReadingStatus.READING);
            if (userBook.getStartDate() == null) {
                userBook.setStartDate(LocalDate.now());
            }
        }

        return toResponse(userBookRepository.save(userBook));
    }

    public UserStatsResponseDTO getUserStats(Long userId){
        List<UserBook> userBooks = userBookRepository.findByUserId(userId);

        return UserStatsResponseDTO.builder()
                .userId(userId)
                .totalBooks(userBooks.size())
                .completedBooks(countByStatus(userBooks, ReadingStatus.COMPLETED))
                .readingBooks(countByStatus(userBooks, ReadingStatus.READING))
                .toReadBooks(countByStatus(userBooks, ReadingStatus.TO_READ))
                .totalPagesRead(userBooks.stream().mapToInt(BookService::pagesReadOf).sum())
                .build();
    }

    /**
     * Loads a tracking record that belongs to the given user.
     *
     * <p>A record owned by somebody else is reported as not found rather than forbidden, so
     * the API does not disclose which ids exist.
     */
    private UserBook requireOwnedUserBook(Long userId, Long userBookId){
        return userBookRepository.findByIdAndUserId(userBookId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tracking record not found with id: " + userBookId));
    }

    private Book newBookFrom(TrackBookRequestDTO request){
        Book book = new Book();
        book.setOpenLibraryId(request.getOpenLibraryId());
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setPageCount(request.getPageCount());
        return book;
    }

    private UserBookResponseDTO toResponse(UserBook userBook){
        Book book = userBook.getBook();
        return UserBookResponseDTO.builder()
                .id(userBook.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .openLibraryId(book.getOpenLibraryId())
                .coverImgUrl(book.getCoverImgUrl())
                .pageCount(book.getPageCount())
                .status(userBook.getStatus())
                .currentPage(userBook.getCurrentPage())
                .startDate(userBook.getStartDate())
                .finishDate(userBook.getFinishDate())
                .build();
    }

    private static long countByStatus(List<UserBook> userBooks, ReadingStatus status){
        return userBooks.stream().filter(userBook -> userBook.getStatus() == status).count();
    }

    private static int pagesReadOf(UserBook userBook){
        Integer pages = (userBook.getStatus() == ReadingStatus.COMPLETED)
                ? userBook.getBook().getPageCount()
                : (userBook.getStatus() == ReadingStatus.READING ? userBook.getCurrentPage() : null);

        return pages != null ? pages : 0;
    }

    private static String firstAuthorOf(List<String> authors){
        return (authors != null && !authors.isEmpty()) ? authors.get(0) : UNKNOWN_AUTHOR;
    }

    private static String coverUrlOf(Integer coverId){
        return coverId != null ? COVER_URL_TEMPLATE.formatted(coverId) : null;
    }
}
