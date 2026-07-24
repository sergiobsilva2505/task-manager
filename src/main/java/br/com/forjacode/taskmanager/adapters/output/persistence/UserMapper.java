package br.com.forjacode.taskmanager.adapters.output.persistence;

import br.com.forjacode.taskmanager.domain.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserJpaEntity toEntity(User user);

    default User toDomain(UserJpaEntity entity) {
        return User.reconstruct(
                entity.getId(),
                entity.getName(),
                entity.getEmail()
        );
    }
}
