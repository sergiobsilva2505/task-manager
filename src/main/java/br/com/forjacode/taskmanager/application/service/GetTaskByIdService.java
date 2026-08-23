package br.com.forjacode.taskmanager.application.service;

import br.com.forjacode.taskmanager.application.ports.input.GetTaskByIdUseCase;
import br.com.forjacode.taskmanager.application.ports.output.TaskRepositoryPort;
import br.com.forjacode.taskmanager.domain.exception.TaskNotFoundException;
import br.com.forjacode.taskmanager.domain.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class GetTaskByIdService implements GetTaskByIdUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetTaskByIdService.class);

    private final TaskRepositoryPort repositoryPort;

    public GetTaskByIdService(TaskRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public Task execute(UUID taskId, UUID currentUserId) {
        Task task = repositoryPort.findById(taskId)
                .filter(t -> currentUserId.equals(t.getOwnerId()))
                .orElseThrow(() -> {
                    log.debug("Task {} not found for user {}", taskId, currentUserId);
                    return new TaskNotFoundException(
                            "Task with ID %s not found for user %s".formatted(taskId, currentUserId));
                });
        log.debug("Task {} found for user {}", taskId, currentUserId);
        return task;
    }

}
