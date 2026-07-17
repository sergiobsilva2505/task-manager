package br.com.forjacode.taskmanager.adapters.output.persistence;

import br.com.forjacode.taskmanager.application.ports.output.TaskRepositoryPort;
import br.com.forjacode.taskmanager.domain.model.Task;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class TaskRepositoryAdapter implements TaskRepositoryPort {

    private final TaskJpaRepository taskJpaRepository;
    private final TaskMapper taskMapper;

    public TaskRepositoryAdapter(TaskJpaRepository taskJpaRepository, TaskMapper taskMapper) {
        this.taskJpaRepository = taskJpaRepository;
        this.taskMapper = taskMapper;
    }

    @Override
    public void save(Task task) {
        TaskJpaEntity entity = taskMapper.toEntity(task);
        taskJpaRepository.save(entity);
    }

    @Override
    public Optional<Task> findById(UUID id) {
        return taskJpaRepository.findById(id).map(taskMapper::toDomain);
    }

    @Override
    public Task update(Task task) {
        TaskJpaEntity entity = taskMapper.toEntity(task);
        TaskJpaEntity updatedEntity = taskJpaRepository.save(entity);
        return taskMapper.toDomain(updatedEntity);
    }

    @Override
    public void delete(Task task) {
        TaskJpaEntity entity = taskMapper.toEntity(task);
        taskJpaRepository.delete(entity);
    }

    @Override
    public List<Task> findAll() {
        return taskJpaRepository.findAll().stream()
                .map(taskMapper::toDomain)
                .collect(Collectors.toList());
    }
}
