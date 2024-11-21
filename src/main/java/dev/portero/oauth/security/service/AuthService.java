package dev.portero.oauth.security.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.portero.oauth.domain.Client;
import dev.portero.oauth.repository.ClientRepository;
import dev.portero.oauth.security.dto.AuthResponse;
import dev.portero.oauth.security.dto.LoginRequest;
import dev.portero.oauth.security.dto.RegistrationRequest;
import dev.portero.oauth.security.model.Token;
import dev.portero.oauth.security.repository.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final ClientRepository repository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegistrationRequest request) {
        Client client = Client.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        Client savedUser = repository.save(client);
        String jwtToken = jwtService.generateToken(client);
        String refreshToken = jwtService.generateRefreshToken(client);

        saveUserToken(savedUser, jwtToken);

        return AuthResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        Client client = repository.findByEmail(request.getEmail()).orElseThrow();
        String jwtToken = jwtService.generateToken(client);
        String refreshToken = jwtService.generateRefreshToken(client);

        revokeAllUserTokens(client);
        saveUserToken(client, jwtToken);

        return AuthResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    private void saveUserToken(Client client, String jwtToken) {
        Token authToken = Token.builder()
                .client(client)
                .token(jwtToken)
                .expired(false)
                .revoked(false)
                .build();

        tokenRepository.save(authToken);
    }

    private void revokeAllUserTokens(Client client) {
        List<Token> validUserAuthTokens = tokenRepository.findAllValidTokenByClientId(client.getId());

        if (validUserAuthTokens.isEmpty()) return;

        validUserAuthTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });

        tokenRepository.saveAll(validUserAuthTokens);
    }

    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String refreshToken;
        String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().build();
        }

        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail != null) {
            Client client = this.repository.findByEmail(userEmail).orElseThrow();

            if (jwtService.isTokenValid(refreshToken, client)) {
                String accessToken = jwtService.generateToken(client);

                this.revokeAllUserTokens(client);
                this.saveUserToken(client, accessToken);

                AuthResponse authResponse = AuthResponse.builder()
                        .token(accessToken)
                        .refreshToken(refreshToken)
                        .build();

                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.badRequest().build();
    }
}
