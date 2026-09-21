package rcm.book;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import rcm.book.config.CorsConfig;
import rcm.book.config.SecurityConfiguration;
import rcm.book.config.TokenProvider;
import rcm.book.controller.BookController;
import rcm.book.dto.UpdateProgressRequestDTO;
import rcm.book.dto.UpdateStatusRequestDTO;
import rcm.book.dto.UserBookResponseDTO;
import rcm.book.dto.UserStatsResponseDTO;
import rcm.book.exception.ResourceNotFoundException;
import rcm.book.model.ReadingStatus;
import rcm.book.model.User;
import rcm.book.service.BookService;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller slice tests.
 *
 * <p>The real {@link SecurityConfiguration} is imported on purpose: besides the filter chain
 * it registers the argument resolver behind {@code @AuthenticationPrincipal}. Without it,
 * Spring MVC would silently bind the {@code User} parameter as an empty model attribute.
 *
 * <p>{@code TokenProvider} and {@code UserDetailsService} are mocked because the
 * {@code JwtAuthenticationFilter} bean belongs to the web slice and requires them.
 */
@WebMvcTest(BookController.class)
@Import({CorsConfig.class, SecurityConfiguration.class})
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private TokenProvider tokenProvider;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private User currentUser;

    @BeforeEach
    void setUp(){
        currentUser = new User(1L, "iris", "iris@email.com", "encoded", null);
    }

    @Test
    void getMyStats_ShouldReturnStatsOfAuthenticatedUser() throws Exception {
        UserStatsResponseDTO stats = UserStatsResponseDTO.builder()
                .userId(1L)
                .totalBooks(3)
                .completedBooks(1)
                .readingBooks(1)
                .toReadBooks(1)
                .totalPagesRead(250)
                .build();
        when(bookService.getUserStats(1L)).thenReturn(stats);

        mockMvc.perform(get("/api/v1/books/me/stats").with(user(currentUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBooks").value(3))
                .andExpect(jsonPath("$.totalPagesRead").value(250));

        // Proves the id came from the JWT principal, not from client input.
        verify(bookService).getUserStats(1L);
    }

    @Test
    void getMyStats_ShouldReturn404_WhenServiceReportsMissingUser() throws Exception {
        when(bookService.getUserStats(1L)).thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/books/me/stats").with(user(currentUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found"))
                .andExpect(jsonPath("$.path").value("/api/v1/books/me/stats"));
    }

    @Test
    void getMyStats_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/books/me/stats"))
                .andExpect(status().isUnauthorized());

        verify(bookService, never()).getUserStats(anyLong());
    }

    @Test
    void updateProgress_ShouldReturn200_WhenRequestValid() throws Exception {
        UserBookResponseDTO response = UserBookResponseDTO.builder()
                .id(1L)
                .title("SICP")
                .currentPage(151)
                .status(ReadingStatus.READING)
                .build();
        when(bookService.updatePageProgress(eq(1L), eq(1L), eq(151))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/books/me/1/progress")
                        .with(user(currentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateProgressRequestDTO(151))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(151));
    }

    @Test
    void updateProgress_ShouldReturn400_WhenPageIsNegative() throws Exception {
        mockMvc.perform(patch("/api/v1/books/me/1/progress")
                        .with(user(currentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateProgressRequestDTO(-5))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(bookService, never()).updatePageProgress(anyLong(), anyLong(), any());
    }

    @Test
    void updateStatus_ShouldReturn200_WhenRequestValid() throws Exception {
        UserBookResponseDTO response = UserBookResponseDTO.builder()
                .id(1L)
                .status(ReadingStatus.COMPLETED)
                .build();
        when(bookService.updateReadingStatus(1L, 1L, ReadingStatus.COMPLETED)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/books/me/1/status")
                        .with(user(currentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateStatusRequestDTO(ReadingStatus.COMPLETED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void searchBooks_ShouldReturn400_WhenQueryIsBlank() throws Exception {
        mockMvc.perform(get("/api/v1/books/search").param("query", " ").with(user(currentUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void corsConfiguration_ShouldAllowAngularOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/books/me/stats")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }
}
