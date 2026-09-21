package rcm.book.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rcm.book.model.ReadingStatus;
import rcm.book.model.UserBook;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBookRepository extends JpaRepository<UserBook, Long> {

    List<UserBook> findByUserId(Long userId);

    List<UserBook> findByUserIdAndStatus(Long userId, ReadingStatus status);

    /**
     * Ownership-scoped lookup: a tracking record is only visible through its owner, so a
     * caller can never reach another user's record even by guessing its id.
     */
    Optional<UserBook> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndBookId(Long userId, Long bookId);
}
