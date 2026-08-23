package br.com.forjacode.taskmanager.adapters.output.security;

import br.com.forjacode.taskmanager.application.ports.output.GoogleUserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class GoogleTokenVerifierAdapterTest {

    private GoogleTokenVerifierAdapter googleTokenVerifierAdapter;

    @BeforeEach
    void setUp() {
        googleTokenVerifierAdapter = new GoogleTokenVerifierAdapter(new GoogleOAuthProperties("test-client-id"));
    }

    @Nested
    @DisplayName("verify")
    class Verify {

        @Test
        @DisplayName("should return empty when token is malformed")
        void shouldReturnEmptyWhenTokenIsMalformed() {
            Optional<GoogleUserInfo> result = googleTokenVerifierAdapter.verify("not-a-valid-jwt");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return empty when token is blank")
        void shouldReturnEmptyWhenTokenIsBlank() {
            Optional<GoogleUserInfo> result = googleTokenVerifierAdapter.verify("");

            assertThat(result).isEmpty();
        }
    }
}
