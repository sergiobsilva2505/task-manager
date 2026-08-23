package br.com.forjacode.taskmanager.adapters.output.persistence;

import br.com.forjacode.taskmanager.adapters.input.rest.mapper.AuthIdentityMapper;
import br.com.forjacode.taskmanager.application.ports.output.AuthIdentityRepositoryPort;
import br.com.forjacode.taskmanager.domain.model.AuthIdentity;
import br.com.forjacode.taskmanager.domain.model.enums.AuthProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class AuthIdentityRepositoryAdapter implements AuthIdentityRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(AuthIdentityRepositoryAdapter.class);

    private final AuthIdentityJpaRepository authIdentityJpaRepository;
    private final AuthIdentityMapper authIdentityMapper;

    public AuthIdentityRepositoryAdapter(AuthIdentityJpaRepository authIdentityJpaRepository,
            AuthIdentityMapper authIdentityMapper) {
        this.authIdentityJpaRepository = authIdentityJpaRepository;
        this.authIdentityMapper = authIdentityMapper;
    }

    @Override
    public void save(AuthIdentity authIdentity) {
        log.info("Saving auth identity for user {} with provider {}", authIdentity.getUserId(),
                authIdentity.getProvider());
        authIdentityJpaRepository.save(authIdentityMapper.toEntity(authIdentity));
        log.info("Auth identity for user {} with provider {} saved successfully", authIdentity.getUserId(),
                authIdentity.getProvider());
    }

    @Override
    public Optional<AuthIdentity> findByUserIdAndProvider(UUID userId, AuthProvider provider) {
        log.debug("Finding auth identity for user {} with provider {}", userId, provider);
        Optional<AuthIdentity> authIdentity = authIdentityJpaRepository.findByUserIdAndProvider(userId, provider)
                .map(authIdentityMapper::toDomain);
        log.info("Finished finding auth identity for user {} with provider {}, found={}", userId, provider,
                authIdentity.isPresent());
        return authIdentity;
    }

    @Override
    public Optional<AuthIdentity> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId) {
        log.debug("Finding auth identity with provider {} and providerUserId {}", provider, providerUserId);
        Optional<AuthIdentity> authIdentity = authIdentityJpaRepository
                .findByProviderAndProviderUserId(provider, providerUserId)
                .map(authIdentityMapper::toDomain);
        log.info("Finished finding auth identity with provider {} and providerUserId {}, found={}", provider,
                providerUserId, authIdentity.isPresent());
        return authIdentity;
    }
}