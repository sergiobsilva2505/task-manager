package br.com.forjacode.taskmanager.adapters.output.persistence;

import br.com.forjacode.taskmanager.application.ports.output.TaskRepositoryPort;
import br.com.forjacode.taskmanager.application.ports.shared.PageQuery;
import br.com.forjacode.taskmanager.application.ports.shared.PagedResult;
import br.com.forjacode.taskmanager.application.ports.shared.SortDirection;
import br.com.forjacode.taskmanager.application.ports.shared.TaskSortField;
import br.com.forjacode.taskmanager.domain.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public List<Task> findAll() {
        return taskJpaRepository.findAll().stream()
                .map(taskMapper::toDomain)
                .toList();
    }

    @Override
    public PagedResult<Task> findAll(PageQuery query, UUID ownerId) {
        Pageable pageable = getPageable(query);

        Page<TaskJpaEntity> tasksPage = taskJpaRepository.findAllByOwnerId(ownerId, pageable);

        List<Task> tasks = tasksPage.getContent()
                .stream()
                .map(taskMapper::toDomain)
                .toList();

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
        taskJpaRepository.deleteByIdAndOwnerId(id, ownerId);
    }

    @Override
    public List<Task> findAllByOwnerId(UUID ownerId) {
        return taskJpaRepository.findAllByOwnerId(ownerId).stream()
                .map(taskMapper::toDomain)
                .toList();
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
