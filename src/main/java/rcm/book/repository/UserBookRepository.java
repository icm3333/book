package rcm.book.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rcm.book.model.ReadingStatus;
import rcm.book.model.UserBook;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBookRepository extends JpaRepository<UserBook, Long> {
    List<UserBook> findByUserId(Long userId); // All books tracked by an id
    List<UserBook> findByUserIdAndStatus(Long userId, ReadingStatus status); // All books tracked by an id with specific status.

    Optional<UserBook> findByUser_IdAndBook_Id(Long userId, Long bookId);
}
