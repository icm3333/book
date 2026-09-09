package rcm.book.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Book {
    @EqualsAndHashCode.Include
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
