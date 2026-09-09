package rcm.book.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rcm.book.config.TokenProvider;
import rcm.book.dto.LoginRequestDTO;
import rcm.book.dto.RegisterRequestDTO;
import rcm.book.dto.TokenResponseDTO;
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
    @Value("${jwt.expiration}")
    private long expirationTime;

    public void register(RegisterRequestDTO dto) throws IllegalArgumentException{
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already in use");
        }

        Roles role = rolesRepository.findByName(UserType.ROLE_USER.name())
                .orElseGet(()-> rolesRepository.save(Roles.builder()
                        .name(UserType.ROLE_USER.name())
                        .build()));

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setRoles(Set.of(role));
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        userRepository.save(user);
    }

    public TokenResponseDTO login(LoginRequestDTO dto) throws Exception{
        try {
            Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));
            String token = tokenProvider.generateToken(auth);
            return new TokenResponseDTO(token, expirationTime);
        }catch (BadCredentialsException e){
            throw new IllegalArgumentException("Bad Credentials");
        }
    }
}
