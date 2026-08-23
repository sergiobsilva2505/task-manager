package br.com.forjacode.taskmanager.adapters.output.security;

import br.com.forjacode.taskmanager.application.ports.output.GeneratedToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenGeneratorAdapterTest {

    private static final String SECRET = "71WqULJsajThyjI+XGqfpL7wUOIT81zI1CX9CUBnWO4=";

    private JwtTokenGeneratorAdapter jwtTokenGeneratorAdapter;

    @BeforeEach
    void setUp() {
        jwtTokenGeneratorAdapter = new JwtTokenGeneratorAdapter(new JwtProperties(SECRET, 60));
    }

    @Nested
    @DisplayName("generate")
    class Generate {

        @Test
        @DisplayName("should generate a token that expires according to the configured expiration minutes")
        void shouldGenerateTokenThatExpiresAccordingToConfiguredExpirationMinutes() {
            UUID userId = UUID.randomUUID();
            Instant before = Instant.now();

            GeneratedToken generatedToken = jwtTokenGeneratorAdapter.generate(userId);

            Instant after = Instant.now();
            assertThat(generatedToken.token()).isNotBlank();
            assertThat(generatedToken.expiresAt())
                    .isAfterOrEqualTo(before.plusSeconds(60 * 60).minusSeconds(5))
                    .isBeforeOrEqualTo(after.plusSeconds(60 * 60).plusSeconds(5));
        }

        @Test
        @DisplayName("should generate a token that validate resolves back to the same user id")
        void shouldGenerateTokenThatValidateResolvesBackToSameUserId() {
            UUID userId = UUID.randomUUID();

            GeneratedToken generatedToken = jwtTokenGeneratorAdapter.generate(userId);

            assertThat(jwtTokenGeneratorAdapter.validate(generatedToken.token())).contains(userId);
        }
    }

    @Nested
    @DisplayName("validate")
    class Validate {

        @Test
        @DisplayName("should return empty when token is malformed")
        void shouldReturnEmptyWhenTokenIsMalformed() {
            Optional<UUID> result = jwtTokenGeneratorAdapter.validate("not-a-valid-jwt");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return empty when token signature does not match the configured secret")
        void shouldReturnEmptyWhenTokenSignatureDoesNotMatchConfiguredSecret() {
            JwtTokenGeneratorAdapter otherSecretAdapter = new JwtTokenGeneratorAdapter(
                    new JwtProperties("uWDPAe0DKJlyOSszVC9n+Xm6hu9GFYegTZEipZEGMlo=", 60));
            GeneratedToken generatedToken = otherSecretAdapter.generate(UUID.randomUUID());

            Optional<UUID> result = jwtTokenGeneratorAdapter.validate(generatedToken.token());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return empty when token is expired")
        void shouldReturnEmptyWhenTokenIsExpired() {
            JwtTokenGeneratorAdapter expiredTokenAdapter = new JwtTokenGeneratorAdapter(
                    new JwtProperties(SECRET, -1));
            GeneratedToken generatedToken = expiredTokenAdapter.generate(UUID.randomUUID());

            Optional<UUID> result = jwtTokenGeneratorAdapter.validate(generatedToken.token());

            assertThat(result).isEmpty();
        }
    }
}