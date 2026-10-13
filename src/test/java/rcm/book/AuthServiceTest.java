package rcm.book;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import rcm.book.config.TokenProvider;
import rcm.book.dto.LoginRequestDTO;
import rcm.book.dto.RegisterRequestDTO;
import rcm.book.dto.TokenResponseDTO;
import rcm.book.exception.DuplicateResourceException;
import rcm.book.model.Roles;
import rcm.book.model.User;
import rcm.book.model.UserType;
import rcm.book.repository.RolesRepository;
import rcm.book.repository.UserRepository;
import rcm.book.service.AuthService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RolesRepository rolesRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private RegisterRequestDTO registerRequest(){
        return RegisterRequestDTO.builder()
                .username("iris")
                .email("iris@email.com")
                .password("senha123")
                .build();
    }

    @Test
    void register_ShouldPersistUserWithEncodedPasswordAndDefaultRole(){
        Roles userRole = Roles.builder().name(UserType.ROLE_USER.name()).build();

        when(userRepository.existsByEmail("iris@email.com")).thenReturn(false);
        when(userRepository.existsByUsername("iris")).thenReturn(false);
        when(rolesRepository.findByName(UserType.ROLE_USER.name())).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("senha123")).thenReturn("hashed");

        authService.register(registerRequest());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertEquals("iris", saved.getUsername());
        assertEquals("iris@email.com", saved.getEmail());
        assertEquals("hashed", saved.getPassword(), "the raw password must never be stored");
        assertTrue(saved.getRoles().contains(userRole));
    }

    @Test
    void register_ShouldCreateDefaultRole_WhenItDoesNotExistYet(){
        when(userRepository.existsByEmail("iris@email.com")).thenReturn(false);
        when(userRepository.existsByUsername("iris")).thenReturn(false);
        when(rolesRepository.findByName(UserType.ROLE_USER.name())).thenReturn(Optional.empty());
        when(rolesRepository.save(any(Roles.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode("senha123")).thenReturn("hashed");

        authService.register(registerRequest());

        verify(rolesRepository).save(any(Roles.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ShouldThrowDuplicateResource_WhenEmailIsTaken(){
        when(userRepository.existsByEmail("iris@email.com")).thenReturn(true);

        DuplicateResourceException exception =
                assertThrows(DuplicateResourceException.class, () -> authService.register(registerRequest()));

        assertTrue(exception.getMessage().contains("Email"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ShouldThrowDuplicateResource_WhenUsernameIsTaken(){
        when(userRepository.existsByEmail("iris@email.com")).thenReturn(false);
        when(userRepository.existsByUsername("iris")).thenReturn(true);

        DuplicateResourceException exception =
                assertThrows(DuplicateResourceException.class, () -> authService.register(registerRequest()));

        assertTrue(exception.getMessage().contains("Username"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_ShouldReturnTokenAndExpiry_WhenCredentialsAreValid(){
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt-token");
        when(tokenProvider.getExpirationMillis()).thenReturn(600000L);

        TokenResponseDTO response = authService.login(new LoginRequestDTO("iris", "senha123"));

        assertEquals("jwt-token", response.token());
        assertEquals(600000L, response.expiresIn());
    }

    @Test
    void login_ShouldPropagateBadCredentials_SoTheHandlerCanReturn401(){
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequestDTO("iris", "wrong")));

        verify(tokenProvider, never()).generateToken(any());
    }
}
