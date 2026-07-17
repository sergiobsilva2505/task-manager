package br.com.forjacode.taskmanager.adapters.output.persistence;

import br.com.forjacode.taskmanager.domain.model.Task;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    TaskJpaEntity toEntity(Task task);

    default Task toDomain(TaskJpaEntity entity) {
        return Task.reconstruct(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getPriority(),
                entity.getDueDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
