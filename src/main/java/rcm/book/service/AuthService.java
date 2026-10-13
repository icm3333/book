package rcm.book.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;

    @Transactional
    public void register(RegisterRequestDTO dto){
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already in use.");
        }
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("Username already in use.");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRoles(Set.of(defaultRole()));

        userRepository.save(user);
    }

    /**
     * Authenticates the credentials and issues a JWT.
     *
     * <p>A failed authentication propagates as {@code BadCredentialsException}, which the
     * global handler renders as 401 with a deliberately generic message.
     */
    public TokenResponseDTO login(LoginRequestDTO dto){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));

        return new TokenResponseDTO(tokenProvider.generateToken(authentication), tokenProvider.getExpirationMillis());
    }

    private Roles defaultRole(){
        return rolesRepository.findByName(UserType.ROLE_USER.name())
                .orElseGet(() -> rolesRepository.save(Roles.builder()
                        .name(UserType.ROLE_USER.name())
                        .build()));
    }
}
