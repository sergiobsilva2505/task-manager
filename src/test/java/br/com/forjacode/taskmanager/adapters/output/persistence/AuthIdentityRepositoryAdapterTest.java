package br.com.forjacode.taskmanager.adapters.output.persistence;

import br.com.forjacode.taskmanager.adapters.input.rest.mapper.AuthIdentityMapper;
import br.com.forjacode.taskmanager.domain.model.AuthIdentity;
import br.com.forjacode.taskmanager.domain.model.enums.AuthProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthIdentityRepositoryAdapterTest {

    @Mock
    private AuthIdentityJpaRepository authIdentityJpaRepository;

    @Mock
    private AuthIdentityMapper authIdentityMapper;

    @InjectMocks
    private AuthIdentityRepositoryAdapter authIdentityRepositoryAdapter;

    private UUID userId;
    private AuthIdentity localAuthIdentity;
    private AuthIdentityJpaEntity entity;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        localAuthIdentity = AuthIdentity.createLocal(userId, "hashed-password");
        entity = new AuthIdentityJpaEntity(localAuthIdentity.getId(), userId, AuthProvider.LOCAL,
                "hashed-password", null, localAuthIdentity.getCreatedAt());
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should save auth identity")
        void shouldSaveAuthIdentity() {
            when(authIdentityMapper.toEntity(localAuthIdentity)).thenReturn(entity);

            authIdentityRepositoryAdapter.save(localAuthIdentity);

            verify(authIdentityJpaRepository).save(entity);
        }
    }

    @Nested
    @DisplayName("findByUserIdAndProvider")
    class FindByUserIdAndProvider {

        @Test
        @DisplayName("should return auth identity when found")
        void shouldReturnAuthIdentityWhenFound() {
            when(authIdentityJpaRepository.findByUserIdAndProvider(userId, AuthProvider.LOCAL))
                    .thenReturn(Optional.of(entity));
            when(authIdentityMapper.toDomain(entity)).thenReturn(localAuthIdentity);

            Optional<AuthIdentity> result = authIdentityRepositoryAdapter
                    .findByUserIdAndProvider(userId, AuthProvider.LOCAL);

            assertThat(result).isPresent().contains(localAuthIdentity);
            verify(authIdentityJpaRepository).findByUserIdAndProvider(userId, AuthProvider.LOCAL);
        }

        @Test
        @DisplayName("should return empty when not found")
        void shouldReturnEmptyWhenNotFound() {
            when(authIdentityJpaRepository.findByUserIdAndProvider(userId, AuthProvider.GOOGLE))
                    .thenReturn(Optional.empty());

            Optional<AuthIdentity> result = authIdentityRepositoryAdapter
                    .findByUserIdAndProvider(userId, AuthProvider.GOOGLE);

            assertThat(result).isEmpty();
            verify(authIdentityJpaRepository).findByUserIdAndProvider(userId, AuthProvider.GOOGLE);
        }
    }

    @Nested
    @DisplayName("findByProviderAndProviderUserId")
    class FindByProviderAndProviderUserId {

        @Test
        @DisplayName("should return auth identity when found")
        void shouldReturnAuthIdentityWhenFound() {
            AuthIdentity googleIdentity = AuthIdentity.createOAuth(userId, AuthProvider.GOOGLE, "google-sub-123");
            AuthIdentityJpaEntity googleEntity = new AuthIdentityJpaEntity(googleIdentity.getId(), userId,
                    AuthProvider.GOOGLE, null, "google-sub-123", googleIdentity.getCreatedAt());

            when(authIdentityJpaRepository.findByProviderAndProviderUserId(AuthProvider.GOOGLE, "google-sub-123"))
                    .thenReturn(Optional.of(googleEntity));
            when(authIdentityMapper.toDomain(googleEntity)).thenReturn(googleIdentity);

            Optional<AuthIdentity> result = authIdentityRepositoryAdapter
                    .findByProviderAndProviderUserId(AuthProvider.GOOGLE, "google-sub-123");

            assertThat(result).isPresent().contains(googleIdentity);
            verify(authIdentityJpaRepository)
                    .findByProviderAndProviderUserId(AuthProvider.GOOGLE, "google-sub-123");
        }

        @Test
        @DisplayName("should return empty when not found")
        void shouldReturnEmptyWhenNotFound() {
            when(authIdentityJpaRepository.findByProviderAndProviderUserId(AuthProvider.GOOGLE, "unknown-sub"))
                    .thenReturn(Optional.empty());

            Optional<AuthIdentity> result = authIdentityRepositoryAdapter
                    .findByProviderAndProviderUserId(AuthProvider.GOOGLE, "unknown-sub");

            assertThat(result).isEmpty();
            verify(authIdentityJpaRepository)
                    .findByProviderAndProviderUserId(AuthProvider.GOOGLE, "unknown-sub");
        }
    }
}

