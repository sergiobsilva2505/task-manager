package br.com.forjacode.taskmanager.application.ports.output;

import br.com.forjacode.taskmanager.application.ports.shared.PageQuery;
import br.com.forjacode.taskmanager.application.ports.shared.PagedResult;
import br.com.forjacode.taskmanager.domain.model.Task;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepositoryPort {

    void save(Task task);

    Optional<Task> findById(UUID id);

    Task update(Task task);

    List<Task> findAll();

    PagedResult<Task> findAll(PageQuery query, UUID ownerId);

    void deleteByIdAndOwnerId(UUID id, UUID ownerId);

    List<Task> findAllByOwnerId(UUID ownerId);
}
