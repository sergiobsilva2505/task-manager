package br.com.forjacode.taskmanager.adapters.output.persistence;

import br.com.forjacode.taskmanager.adapters.input.rest.mapper.AuthIdentityMapper;
import br.com.forjacode.taskmanager.application.ports.output.AuthIdentityRepositoryPort;
import br.com.forjacode.taskmanager.domain.model.AuthIdentity;
import br.com.forjacode.taskmanager.domain.model.enums.AuthProvider;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class AuthIdentityRepositoryAdapter implements AuthIdentityRepositoryPort {

    private final AuthIdentityJpaRepository authIdentityJpaRepository;
    private final AuthIdentityMapper authIdentityMapper;

    public AuthIdentityRepositoryAdapter(AuthIdentityJpaRepository authIdentityJpaRepository,
            AuthIdentityMapper authIdentityMapper) {
        this.authIdentityJpaRepository = authIdentityJpaRepository;
        this.authIdentityMapper = authIdentityMapper;
    }

    @Override
    public void save(AuthIdentity authIdentity) {
        authIdentityJpaRepository.save(authIdentityMapper.toEntity(authIdentity));
    }

    @Override
    public Optional<AuthIdentity> findByUserIdAndProvider(UUID userId, AuthProvider provider) {
        return authIdentityJpaRepository.findByUserIdAndProvider(userId, provider)
                .map(authIdentityMapper::toDomain);
    }

    @Override
    public Optional<AuthIdentity> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId) {
        return authIdentityJpaRepository.findByProviderAndProviderUserId(provider, providerUserId)
                .map(authIdentityMapper::toDomain);
    }
}