package br.com.forjacode.taskmanager.application.ports.output;

import br.com.forjacode.taskmanager.domain.model.AuthIdentity;
import br.com.forjacode.taskmanager.domain.model.enums.AuthProvider;

import java.util.Optional;
import java.util.UUID;

public interface AuthIdentityRepositoryPort {

    void save(AuthIdentity authIdentity);

    Optional<AuthIdentity> findByUserIdAndProvider(UUID userId, AuthProvider provider);

    Optional<AuthIdentity> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
}