package rcm.book.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "open_library_id", nullable = false, unique = true)
    private String openLibraryId;

    @Column(nullable = false)
    private String title;

    private String author;

    @Column(name = "cover_img_url")
    private String coverImgURL;

    @Column(name = "page_count")
    private Integer pageCount;
}
