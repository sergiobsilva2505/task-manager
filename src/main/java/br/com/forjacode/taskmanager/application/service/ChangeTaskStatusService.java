package br.com.forjacode.taskmanager.application.service;

import br.com.forjacode.taskmanager.application.ports.input.ChangeTaskStatusUseCase;
import br.com.forjacode.taskmanager.application.ports.input.command.ChangeTaskStatusCommand;
import br.com.forjacode.taskmanager.application.ports.output.TaskRepositoryPort;
import br.com.forjacode.taskmanager.domain.exception.TaskNotFoundException;
import br.com.forjacode.taskmanager.domain.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangeTaskStatusService implements ChangeTaskStatusUseCase {

    private static final Logger log = LoggerFactory.getLogger(ChangeTaskStatusService.class);

    private final TaskRepositoryPort repositoryPort;

    public ChangeTaskStatusService(TaskRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public Task execute(ChangeTaskStatusCommand command) {
        Task task = repositoryPort.findById(command.taskId())
                .filter(t -> t.getOwnerId().equals(command.ownerId()))
                .orElseThrow(() -> {
                    log.debug("Task {} not found for user {}", command.taskId(), command.ownerId());
                    return new TaskNotFoundException(
                            "Task with ID %s not found for user %s".formatted(command.taskId(), command.ownerId()));
                });

        task.changeStatus(command.newStatus());

        log.info("Updating task {} status to {} for user {}", task.getId(), task.getStatus(), command.ownerId());
        Task updatedTask = repositoryPort.update(task);

        log.info("Task {} status changed to {} by user {}", updatedTask.getId(), updatedTask.getStatus(),
                command.ownerId());

        return updatedTask;
    }
}