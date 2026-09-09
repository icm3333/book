package rcm.book.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rcm.book.client.OpenLibraryClient;
import rcm.book.dto.BookSearchResultDTO;
import rcm.book.dto.OpenLibraryResponseDTO;
import rcm.book.dto.UserBookResponseDTO;
import rcm.book.dto.UserStatsResponseDTO;
import rcm.book.exception.DuplicateResourceException;
import rcm.book.exception.ResourceNotFoundException;
import rcm.book.repository.BookRepository;
import rcm.book.repository.UserBookRepository;
import rcm.book.repository.UserRepository;
import rcm.book.model.Book;
import rcm.book.model.ReadingStatus;
import rcm.book.model.User;
import rcm.book.model.UserBook;

import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {
    private final BookRepository bookRepository;
    private final UserBookRepository userBookRepository;
    private final UserRepository userRepository;
    private final OpenLibraryClient openLibraryClient;

    private UserBookResponseDTO mapUserBookToDTO(UserBook userBook){
        return UserBookResponseDTO.builder()
                .id(userBook.getId())
                .title(userBook.getBook().getTitle())
                .author(userBook.getBook().getAuthor())
                .openLibraryId(userBook.getBook().getOpenLibraryId())
                .coverImgURL(userBook.getBook().getCoverImgURL())
                .pageCount(userBook.getBook().getPageCount())
                .status(userBook.getStatus())
                .currentPage(userBook.getCurrentPage())
                .startDate(userBook.getStartDate())
                .finishDate(userBook.getFinishDate()).build();
    }

    public List<BookSearchResultDTO> searchBooks(String query){
        if(query == null || query.trim().isEmpty()){
            throw new IllegalArgumentException("Search query is empty.");
        }

        OpenLibraryResponseDTO response = openLibraryClient.searchBooks(query);

        if(response == null || response.getDocs() == null){
            return List.of();
        }

        return response.getDocs().stream().map(bookDto -> {
            String authorName = (bookDto.getAuthor() != null && !bookDto.getAuthor().isEmpty())
                                ? bookDto.getAuthor().get(0)
                                : "Unknown";
            String coverUrl = (bookDto.getCoverId() != null)
                                ? "https://covers.openLibrary.org/b/id/" + bookDto.getCoverId() + "-M.jpg"
                                : null;

            return BookSearchResultDTO.builder()
                    .openLibraryId(bookDto.getKey())
                    .title(bookDto.getTitle())
                    .author(authorName)
                    .coverImgUrl(coverUrl)
                    .pageCount(bookDto.getNumberOfPagesMedian())
                    .build();
        }).toList();
    }

    @Transactional
    public UserBookResponseDTO addBookToUserList(Long userId, String openLibraryId, String title, String author, Integer pageCount){
        User user = userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User not found with id: " + userId));

        Book book = bookRepository.findByOpenLibraryId(openLibraryId)
                .orElseGet(()-> {
                   Book newBook = new Book();
                   newBook.setOpenLibraryId(openLibraryId);
                   newBook.setTitle(title);
                   newBook.setAuthor(author);
                   newBook.setPageCount(pageCount);
                   return bookRepository.save(newBook);
                });

        if(userBookRepository.findByUser_IdAndBook_Id(userId, book.getId()).ifPresent()){
            throw new DuplicateResourceException("Book is already in this user's list.");
        }

        UserBook userBook = new UserBook();
        userBook.setUser(user);
        userBook.setBook(book);
        userBook.setStatus(ReadingStatus.TO_READ);
        userBook.setCurrentPage(0);

        return mapUserBookToDTO(userBookRepository.save(userBook));
    }

    public List<UserBookResponseDTO> getUserBooks(Long userId, ReadingStatus status){
        List<UserBook> userBooks;
        if(status != null){
            userBooks =  userBookRepository.findByUserIdAndStatus(userId, status);
        }else{
            userBooks = userBookRepository.findByUserId(userId);
        }
        return userBooks.stream().map(this::mapUserBookToDTO).toList();
    }

    @Transactional
    public UserBookResponseDTO updateReadingStatus(Long userBookId, ReadingStatus newStatus){
        UserBook userBook = userBookRepository.findById(userBookId)
                .orElseThrow(()-> new ResourceNotFoundException("Tracking record not found with id: " + userBookId));

        userBook.setStatus(newStatus);

        if(newStatus == ReadingStatus.READING && userBook.getStartDate() == null){
            // if just started reading set today date as start reading date.
            userBook.setStartDate(LocalDate.now());
        }else if(newStatus == ReadingStatus.COMPLETED){
            // if they moved from TO_READ to COMPLETED set startDate and finishDate as today.
            if(userBook.getStartDate() == null) userBook.setStartDate(LocalDate.now());
            userBook.setFinishDate(LocalDate.now());
            if(userBook.getBook().getPageCount() != null) userBook.setCurrentPage(userBook.getBook().getPageCount());
        }

        return mapUserBookToDTO(userBookRepository.save(userBook));
    }

    @Transactional
    public UserBookResponseDTO updatePageProgress(Long userBookId, Integer newPage){
        UserBook userBook = userBookRepository.findById(userBookId)
                .orElseThrow(()-> new ResourceNotFoundException("Tracking record not found with id: " + userBookId));

        if(newPage < 0){
            throw new IllegalArgumentException("Page cannot be negative.");
        }

        Integer totalPages = userBook.getBook().getPageCount();
        if(totalPages != null && newPage > totalPages){
            throw new IllegalArgumentException("Current page cannot be greater than total book pages (" + totalPages + ").");
        }
        userBook.setCurrentPage(newPage);

        // if user started reading and only changed the page count, move from TO_READ to READING
        if(newPage > 0 && userBook.getStatus() == ReadingStatus.TO_READ){
            userBook.setStatus(ReadingStatus.READING);
            if(userBook.getStartDate() == null){
                userBook.setStartDate(LocalDate.now());
            }
        }
        return mapUserBookToDTO(userBookRepository.save(userBook));
    }

    public UserStatsResponseDTO getUserStats(Long userId){
        userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User with id " + userId + " was not found"));

        List<UserBook> userBooks = userBookRepository.findByUserId(userId);

        long completed = userBooks.stream().filter(ub -> ub.getStatus() == ReadingStatus.COMPLETED).count();
        long reading = userBooks.stream().filter(ub -> ub.getStatus() == ReadingStatus.READING).count();
        long toRead = userBooks.stream().filter(ub -> ub.getStatus() == ReadingStatus.TO_READ).count();

        int totalPagesRead = userBooks.stream().mapToInt(ub -> {
            if(ub.getStatus() == ReadingStatus.COMPLETED){
                return (ub.getBook().getPageCount() != null) ? ub.getBook().getPageCount() : 0;
            }else if(ub.getStatus() == ReadingStatus.READING){
                return (ub.getCurrentPage() != null) ? ub.getCurrentPage() : 0;
            }else {
                return 0;
            }
        }).sum();

        return UserStatsResponseDTO.builder()
                .userId(userId)
                .totalBooks(userBooks.size())
                .completedBooks(completed)
                .readingBooks(reading)
                .toReadBooks(toRead)
                .totalPagesRead(totalPagesRead)
                .build();
    }

}
