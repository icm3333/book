package rcm.book.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rcm.book.model.Roles;

import java.util.Optional;

public interface RolesRepository extends JpaRepository<Roles, Integer> {
    Optional<Roles> findByName(String role);
}
