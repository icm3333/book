package rcm.book;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import rcm.book.config.CorsConfig;
import rcm.book.config.SecurityConfiguration;
import rcm.book.config.TokenProvider;
import rcm.book.controller.AuthController;
import rcm.book.dto.LoginRequestDTO;
import rcm.book.dto.RegisterRequestDTO;
import rcm.book.dto.TokenResponseDTO;
import rcm.book.exception.DuplicateResourceException;
import rcm.book.service.AuthService;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the auth endpoints are publicly reachable and return the documented status codes.
 */
@WebMvcTest(AuthController.class)
@Import({CorsConfig.class, SecurityConfiguration.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private TokenProvider tokenProvider;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void register_ShouldReturn201_WhenRequestIsValid() throws Exception {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .username("iris")
                .email("iris@email.com")
                .password("senha123")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(authService).register(any(RegisterRequestDTO.class));
    }

    @Test
    void register_ShouldReturn409_WhenEmailAlreadyExists() throws Exception {
        doThrow(new DuplicateResourceException("Email already in use."))
                .when(authService).register(any(RegisterRequestDTO.class));

        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .username("iris")
                .email("iris@email.com")
                .password("senha123")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Email already in use."));
    }

    @Test
    void register_ShouldReturn400_WhenFieldsAreBlank() throws Exception {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .username("")
                .email("")
                .password("")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(authService, never()).register(any(RegisterRequestDTO.class));
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValid() throws Exception {
        when(authService.login(any(LoginRequestDTO.class)))
                .thenReturn(new TokenResponseDTO("jwt-token", 600000L));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequestDTO("iris", "senha123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.expiresIn").value(600000L));
    }

    @Test
    void login_ShouldReturn401_WhenCredentialsAreInvalid() throws Exception {
        when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequestDTO("iris", "wrong"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                // The message must not reveal whether the username exists.
                .andExpect(jsonPath("$.message").value("Invalid username or password."));
    }
}
