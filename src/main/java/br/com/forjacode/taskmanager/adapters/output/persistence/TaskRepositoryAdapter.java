package br.com.forjacode.taskmanager.adapters.output.persistence;

import br.com.forjacode.taskmanager.application.ports.output.TaskRepositoryPort;
import br.com.forjacode.taskmanager.application.ports.shared.PageQuery;
import br.com.forjacode.taskmanager.application.ports.shared.PagedResult;
import br.com.forjacode.taskmanager.application.ports.shared.SortDirection;
import br.com.forjacode.taskmanager.application.ports.shared.TaskSortField;
import br.com.forjacode.taskmanager.domain.model.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Slf4j
public class TaskRepositoryAdapter implements TaskRepositoryPort {

    private final TaskJpaRepository taskJpaRepository;
    private final TaskMapper taskMapper;

    public TaskRepositoryAdapter(TaskJpaRepository taskJpaRepository, TaskMapper taskMapper) {
        this.taskJpaRepository = taskJpaRepository;
        this.taskMapper = taskMapper;
    }

    @Override
    public void save(Task task) {
        log.info("Saving task with id {}", task.getId());
        TaskJpaEntity entity = taskMapper.toEntity(task);
        taskJpaRepository.save(entity);
        log.info("Task with id {} saved successfully", task.getId());
    }

    @Override
    public Optional<Task> findById(UUID id) {
        log.info("Finding task with id {}", id);
        return taskJpaRepository.findById(id).map(taskMapper::toDomain);

    }

    @Override
    public Task update(Task task) {
        log.info("Updating task with id {}", task.getId());
        TaskJpaEntity entity = taskMapper.toEntity(task);
        TaskJpaEntity updatedEntity = taskJpaRepository.save(entity);
        log.info("Task with id {} updated successfully", task.getId());
        return taskMapper.toDomain(updatedEntity);
    }

    @Override
    public List<Task> findAll() {
        log.info("Finding all tasks");
        return taskJpaRepository.findAll().stream()
                .map(taskMapper::toDomain)
                .toList();
    }

    @Override
    public PagedResult<Task> findAll(PageQuery query, UUID ownerId) {
        log.info("Finding all tasks for owner with id {}", ownerId);
        Pageable pageable = getPageable(query);

        Page<TaskJpaEntity> tasksPage = taskJpaRepository.findAllByOwnerId(ownerId, pageable);

        List<Task> tasks = tasksPage.getContent()
                .stream()
                .map(taskMapper::toDomain)
                .toList();

        log.info("Found {} tasks for owner with id {}", tasks.size(), ownerId);

        return new PagedResult<>(
                tasks,
                tasksPage.getNumber(),
                tasksPage.getSize(),
                tasksPage.getTotalElements(),
                tasksPage.getTotalPages()
        );
    }

    @Override
    public void deleteByIdAndOwnerId(UUID id, UUID ownerId) {
        log.info("Deleting task with id {} for owner with id {}", id, ownerId);
        taskJpaRepository.deleteByIdAndOwnerId(id, ownerId);
        log.info("Task with id {} for owner with id {} deleted successfully", id, ownerId);
    }

    @Override
    public List<Task> findAllByOwnerId(UUID ownerId) {
        log.info("Finding all tasks for owner with id {} paged", ownerId);
        List<Task> tasks = taskJpaRepository.findAllByOwnerId(ownerId).stream()
                .map(taskMapper::toDomain)
                .toList();
        log.info("Found {} tasks for owner with id {}", tasks.size(), ownerId);
        return tasks;
    }

    private String getJpaFieldName(TaskSortField fieldName) {
        return switch (fieldName) {
            case TITLE -> "title";
            case CREATED_AT -> "createdAt";
            case DUE_DATE -> "dueDate";
            case PRIORITY -> "priority";
        };
    }

    private Pageable getPageable(PageQuery query) {
        Sort.Direction direction = query.sortDirection() == SortDirection.DESC
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return PageRequest.of(query.page(), query.size(), direction, getJpaFieldName(query.sortBy()));
    }
}
