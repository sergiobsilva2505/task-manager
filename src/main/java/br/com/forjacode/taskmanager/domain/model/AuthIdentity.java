package br.com.forjacode.taskmanager.domain.model;

import br.com.forjacode.taskmanager.domain.exception.InvalidAuthIdentityException;
import br.com.forjacode.taskmanager.domain.exception.MissingRequiredFieldException;
import br.com.forjacode.taskmanager.domain.model.enums.AuthProvider;

import java.time.Instant;
import java.util.UUID;

@SuppressWarnings("LombokGetterMayBeUsed")
public class AuthIdentity {

    private final UUID id;
    private final UUID userId;
    private final AuthProvider provider;
    private final String passwordHash;
    private final String providerUserId;
    private final Instant createdAt;

    private AuthIdentity(UUID id, UUID userId, AuthProvider provider, String passwordHash,
            String providerUserId, Instant createdAt) {

        validateRequiredFields(userId, provider, createdAt);
        validateProviderSpecificFields(provider, passwordHash, providerUserId);

        this.id = id;
        this.userId = userId;
        this.provider = provider;
        this.passwordHash = passwordHash;
        this.providerUserId = providerUserId;
        this.createdAt = createdAt;
    }

    public static AuthIdentity createLocal(UUID userId, String passwordHash) {
        return new AuthIdentity(UUID.randomUUID(), userId, AuthProvider.LOCAL, passwordHash, null, Instant.now());
    }

    public static AuthIdentity createOAuth(UUID userId, AuthProvider provider, String providerUserId) {
        return new AuthIdentity(UUID.randomUUID(), userId, provider, null, providerUserId, Instant.now());
    }

    public static AuthIdentity reconstruct(UUID id, UUID userId, AuthProvider provider, String passwordHash,
            String providerUserId, Instant createdAt) {
        return new AuthIdentity(id, userId, provider, passwordHash, providerUserId, createdAt);
    }

    private static void validateRequiredFields(UUID userId, AuthProvider provider, Instant createdAt) {
        if (userId == null) {
            throw new MissingRequiredFieldException("userId cannot be null");
        }
        if (provider == null) {
            throw new MissingRequiredFieldException("provider cannot be null");
        }
        if (createdAt == null) {
            throw new MissingRequiredFieldException("createdAt cannot be null");
        }
    }

    private static void validateProviderSpecificFields(AuthProvider provider, String passwordHash,
            String providerUserId) {
        boolean hasPassword = passwordHash != null && !passwordHash.isEmpty();
        boolean hasProviderUserId = providerUserId != null && !providerUserId.isEmpty();

        if (provider == AuthProvider.LOCAL) {
            if (!hasPassword) {
                throw new InvalidAuthIdentityException("passwordHash is required for LOCAL provider");
            }
            if (hasProviderUserId) {
                throw new InvalidAuthIdentityException("providerUserId must be null or empty for LOCAL provider");
            }
        }

        if (provider == AuthProvider.GOOGLE) {
            if (hasPassword) {
                throw new InvalidAuthIdentityException("passwordHash must be null or empty for GOOGLE provider");
            }
            if (!hasProviderUserId) {
                throw new InvalidAuthIdentityException("providerUserId is required for GOOGLE provider");
            }
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public AuthProvider getProvider() {
        return provider;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getProviderUserId() {
        return providerUserId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}