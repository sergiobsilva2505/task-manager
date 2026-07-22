package br.com.forjacode.taskmanager.application.service;

import br.com.forjacode.taskmanager.application.ports.input.ChangeTaskStatusUseCase;
import br.com.forjacode.taskmanager.application.ports.input.command.ChangeTaskStatusCommand;
import br.com.forjacode.taskmanager.application.ports.output.TaskRepositoryPort;
import br.com.forjacode.taskmanager.domain.exception.TaskNotFoundException;
import br.com.forjacode.taskmanager.domain.model.Task;

public class ChangeTaskStatusService implements ChangeTaskStatusUseCase {

    private final TaskRepositoryPort repositoryPort;

    public ChangeTaskStatusService(TaskRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public Task execute(ChangeTaskStatusCommand command) {
        Task task = repositoryPort.findById(command.taskId())
                .orElseThrow(() -> new TaskNotFoundException("Task with ID %s not found".formatted(command.taskId())));

        task.changeStatus(command.newStatus());
        return repositoryPort.update(task);
    }
}
