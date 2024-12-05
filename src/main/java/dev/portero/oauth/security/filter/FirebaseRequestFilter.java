package dev.portero.oauth.security.filter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import dev.portero.oauth.domain.Client;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class FirebaseRequestFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_URL = "/api/v1/auth";

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws IOException {
        try {
            if (request.getServletPath().contains(AUTHORIZATION_URL)) {
                filterChain.doFilter(request, response);
                return;
            }

            String jwt = this.extractJwtFromRequest(request);

            if (jwt == null) {
                filterChain.doFilter(request, response);
                return;
            }

            this.processFirebaseAuth(jwt);
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            log.error("Token is expired", e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is expired");
        } catch (Exception e) {
            log.error("Invalid token", e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
        }
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private void processFirebaseAuth(String jwt) {
        try {
            FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(jwt);

            String userId = token.getUid();
            String email = token.getEmail();

            Client userDetails = Client.builder()
                    .username(email != null ? email : userId)
                    .build();

            this.setAuthenticationContext(userDetails);
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Firebase token verification failed", e);
        }
    }

    private void setAuthenticationContext(UserDetails userDetails) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
