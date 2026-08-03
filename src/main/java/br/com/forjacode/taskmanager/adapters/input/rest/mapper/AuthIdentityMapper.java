package br.com.forjacode.taskmanager.adapters.input.rest.mapper;

import br.com.forjacode.taskmanager.adapters.output.persistence.AuthIdentityJpaEntity;
import br.com.forjacode.taskmanager.domain.model.AuthIdentity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthIdentityMapper {

    AuthIdentityJpaEntity toEntity(AuthIdentity authIdentity);

    default AuthIdentity toDomain(AuthIdentityJpaEntity entity) {
        return AuthIdentity.reconstruct(
                entity.getId(),
                entity.getUserId(),
                entity.getProvider(),
                entity.getPasswordHash(),
                entity.getProviderUserId(),
                entity.getCreatedAt()
        );
    }
}
