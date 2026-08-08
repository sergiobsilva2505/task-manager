package br.com.forjacode.taskmanager.domain.model;

import br.com.forjacode.taskmanager.domain.exception.InvalidAuthIdentityException;
import br.com.forjacode.taskmanager.domain.exception.MissingRequiredFieldException;
import br.com.forjacode.taskmanager.domain.model.enums.AuthProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("java:S2187") // Suppressing "Test class does not contain any tests" warning because the nested classes contain the actual tests
class AuthIdentityTest {

    @Nested
    @DisplayName("Success")
    class Success {

        @Test
        @DisplayName("Should create local auth identity with valid data")
        void shouldCreateLocalAuthIdentityWithValidData() {
            UUID userId = UUID.randomUUID();
            String passwordHash = "hash123";

            AuthIdentity authIdentity = AuthIdentity.createLocal(userId, passwordHash);

            assertThat(authIdentity.getId()).isNotNull();
            assertThat(authIdentity.getUserId()).isEqualTo(userId);
            assertThat(authIdentity.getProvider()).isEqualTo(AuthProvider.LOCAL);
            assertThat(authIdentity.getPasswordHash()).isEqualTo(passwordHash);
            assertThat(authIdentity.getProviderUserId()).isNull();
            assertThat(authIdentity.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should create OAuth auth identity with valid data")
        void shouldCreateOAuthAuthIdentityWithValidData() {
            UUID userId = UUID.randomUUID();
            String providerUserId = "google-sub-123";

            AuthIdentity authIdentity = AuthIdentity.createOAuth(userId, AuthProvider.GOOGLE, providerUserId);

            assertThat(authIdentity.getId()).isNotNull();
            assertThat(authIdentity.getUserId()).isEqualTo(userId);
            assertThat(authIdentity.getProvider()).isEqualTo(AuthProvider.GOOGLE);
            assertThat(authIdentity.getPasswordHash()).isNull();
            assertThat(authIdentity.getProviderUserId()).isEqualTo(providerUserId);
            assertThat(authIdentity.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should reconstruct auth identity with persisted data")
        void shouldReconstructAuthIdentityWithPersistedData() {
            UUID id = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            String passwordHash = "hash123";
            Instant createdAt = Instant.now();

            AuthIdentity authIdentity = AuthIdentity.reconstruct(id, userId, AuthProvider.LOCAL, passwordHash, null,
                    createdAt);

            assertThat(authIdentity.getId()).isEqualTo(id);
            assertThat(authIdentity.getUserId()).isEqualTo(userId);
            assertThat(authIdentity.getProvider()).isEqualTo(AuthProvider.LOCAL);
            assertThat(authIdentity.getPasswordHash()).isEqualTo(passwordHash);
            assertThat(authIdentity.getProviderUserId()).isNull();
            assertThat(authIdentity.getCreatedAt()).isEqualTo(createdAt);
        }
    }

    @Nested
    class WithError {

        @Test
        void shouldThrowExceptionWhenUserIdIsNull() {
            assertThatThrownBy(
                    () -> AuthIdentity.reconstruct(UUID.randomUUID(), null, AuthProvider.LOCAL, "hash123", null,
                            Instant.now()))
                    .isInstanceOf(MissingRequiredFieldException.class)
                    .hasMessage("userId cannot be null");
        }

        @Test
        void shouldThrowExceptionWhenProviderIsNull() {
            assertThatThrownBy(
                    () -> AuthIdentity.reconstruct(UUID.randomUUID(), UUID.randomUUID(), null, "hash123", null,
                            Instant.now()))
                    .isInstanceOf(MissingRequiredFieldException.class)
                    .hasMessage("provider cannot be null");
        }

        @Test
        void shouldThrowExceptionWhenCreatedAtIsNull() {
            assertThatThrownBy(
                    () -> AuthIdentity.reconstruct(UUID.randomUUID(), UUID.randomUUID(), AuthProvider.LOCAL, "hash123",
                            null, null))
                    .isInstanceOf(MissingRequiredFieldException.class)
                    .hasMessage("createdAt cannot be null");
        }

        @Test
        void shouldThrowExceptionWhenLocalProviderHasNoPassword() {
            assertThatThrownBy(
                    () -> AuthIdentity.reconstruct(UUID.randomUUID(), UUID.randomUUID(), AuthProvider.LOCAL, null, null,
                            Instant.now()))
                    .isInstanceOf(InvalidAuthIdentityException.class)
                    .hasMessage("passwordHash is required for LOCAL provider");
        }

        @Test
        void shouldThrowExceptionWhenLocalProviderHasProviderUserId() {
            assertThatThrownBy(
                    () -> AuthIdentity.reconstruct(UUID.randomUUID(), UUID.randomUUID(), AuthProvider.LOCAL, "hash123",
                            "provider-id", Instant.now()))
                    .isInstanceOf(InvalidAuthIdentityException.class)
                    .hasMessage("providerUserId must be null or empty for LOCAL provider");
        }

        @Test
        void shouldThrowExceptionWhenGoogleProviderHasPassword() {
            assertThatThrownBy(
                    () -> AuthIdentity.reconstruct(UUID.randomUUID(), UUID.randomUUID(), AuthProvider.GOOGLE, "hash123",
                            "google-sub-123", Instant.now()))
                    .isInstanceOf(InvalidAuthIdentityException.class)
                    .hasMessage("passwordHash must be null or empty for GOOGLE provider");
        }

        @Test
        void shouldThrowExceptionWhenGoogleProviderHasNoProviderUserId() {
            assertThatThrownBy(
                    () -> AuthIdentity.reconstruct(UUID.randomUUID(), UUID.randomUUID(), AuthProvider.GOOGLE, null,
                            null, Instant.now()))
                    .isInstanceOf(InvalidAuthIdentityException.class)
                    .hasMessage("providerUserId is required for GOOGLE provider");
        }
    }

}