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
        return repositoryPort.findById(command.taskId()).map(task -> {
            task.changeStatus(command.newStatus());
            repositoryPort.update(task);
            return task;
        }).orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + command.taskId()));
    }
}
