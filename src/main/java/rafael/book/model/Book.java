package rafael.book.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tb_books")
@Data
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String author;
    private Integer totalPages;
    private Integer currentPage;
    private String status;
}
