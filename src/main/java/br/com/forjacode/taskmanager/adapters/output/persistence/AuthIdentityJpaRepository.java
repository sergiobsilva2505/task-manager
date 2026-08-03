package br.com.forjacode.taskmanager.adapters.output.persistence;

import br.com.forjacode.taskmanager.domain.model.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthIdentityJpaRepository extends JpaRepository<AuthIdentityJpaEntity, UUID> {

    Optional<AuthIdentityJpaEntity> findByUserIdAndProvider(UUID userId, AuthProvider provider);
}
