package rcm.book;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import rcm.book.config.CorsConfig;
import rcm.book.controller.BookController;
import rcm.book.dto.UserBookResponseDTO;
import rcm.book.dto.UserStatsResponseDTO;
import rcm.book.exception.ResourceNotFoundException;
import rcm.book.model.ReadingStatus;
import rcm.book.service.BookService;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(BookController.class)
@Import(CorsConfig.class)
public class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;

    @Test
    void getUserStats_ShouldReturn200_IfExists() throws Exception{
        UserStatsResponseDTO mockStats = mock(UserStatsResponseDTO.class);
        when(bookService.getUserStats(1L)).thenReturn(mockStats);

        mockMvc.perform(get("/api/v1/books/user/1/stats"))
                .andExpect(status().isOk());
    }

    @Test
    void getUserStats_ShouldReturn404_IfNotExist() throws Exception{
        when(bookService.getUserStats(67L)).thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/books/user/67/stats"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProgress_ShouldReturn200_IfRequestValid() throws Exception{
        UserBookResponseDTO mockResponse = mock(UserBookResponseDTO.class);

        when(bookService.updatePageProgress(eq(1L), eq(151)))
                .thenReturn(mockResponse);

        mockMvc.perform(patch("/api/v1/books/tracking/1/progress")
                .param("page", "151"))
                .andExpect(status().isOk());
    }

    @Test
    void corsConfiguration_ShouldAllowAngularOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/books/users/1/stats") // OPTIONS preflight request
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }
}
