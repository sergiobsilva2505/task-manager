package br.com.forjacode.taskmanager.adapters.input.rest.security;

import br.com.forjacode.taskmanager.application.ports.output.TokenGeneratorPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@NullMarked
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenGeneratorPort tokenGeneratorPort;

    public JwtAuthenticationFilter(TokenGeneratorPort tokenGeneratorPort) {
        this.tokenGeneratorPort = tokenGeneratorPort;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        extractToken(request).ifPresent(this::validateAndAuthenticate);

        filterChain.doFilter(request, response);
    }

    private void validateAndAuthenticate(String token) {
        tokenGeneratorPort.validate(token)
                .ifPresentOrElse(
                        this::authenticate,
                        () -> log.warn("Rejected invalid or expired JWT token")
                );
    }

    private Optional<String> extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return Optional.empty();
        }
        return Optional.of(header.substring(BEARER_PREFIX.length()));
    }

    private void authenticate(UUID userId) {
        var authentication = new UsernamePasswordAuthenticationToken(userId, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}