package br.com.forjacode.taskmanager.application.ports.output;

import br.com.forjacode.taskmanager.domain.model.Task;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepositoryPort {

    void save(Task task);

    Optional<Task> findById(UUID id);

    Task update(Task task);

    void delete(Task task);

    List<Task> findAll();
}
