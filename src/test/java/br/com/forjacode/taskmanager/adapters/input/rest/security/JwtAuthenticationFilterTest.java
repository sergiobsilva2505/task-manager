package br.com.forjacode.taskmanager.adapters.input.rest.security;

import br.com.forjacode.taskmanager.application.ports.output.TokenGeneratorPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private TokenGeneratorPort tokenGeneratorPort;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(tokenGeneratorPort);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("Success")
    class Success {

        @Test
        @DisplayName("should authenticate when Authorization header has a valid Bearer token")
        void shouldAuthenticateWhenAuthorizationHeaderHasAValidBearerToken() throws Exception {
            UUID userId = UUID.randomUUID();
            when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
            when(tokenGeneratorPort.validate("valid-token")).thenReturn(Optional.of(userId));

            filter.doFilter(request, response, filterChain);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNotNull();
            assertThat(authentication.getPrincipal()).isEqualTo(userId);
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("WithError")
    class WithError {

        @Test
        @DisplayName("should not authenticate when Authorization header is missing")
        void shouldNotAuthenticateWhenAuthorizationHeaderIsMissing() throws Exception {
            when(request.getHeader("Authorization")).thenReturn(null);

            filter.doFilter(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should not authenticate when Authorization header does not start with Bearer")
        void shouldNotAuthenticateWhenAuthorizationHeaderDoesNotStartWithBearer() throws Exception {
            when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

            filter.doFilter(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should not authenticate when token is invalid")
        void shouldNotAuthenticateWhenTokenIsInvalid() throws Exception {
            when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
            when(tokenGeneratorPort.validate("invalid-token")).thenReturn(Optional.empty());

            filter.doFilter(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(filterChain).doFilter(request, response);
        }
    }
}