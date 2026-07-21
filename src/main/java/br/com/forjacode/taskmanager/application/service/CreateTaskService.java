package br.com.forjacode.taskmanager.application.service;

import br.com.forjacode.taskmanager.application.ports.input.CreateTaskUseCase;
import br.com.forjacode.taskmanager.application.ports.input.command.CreateTaskCommand;
import br.com.forjacode.taskmanager.application.ports.output.TaskRepositoryPort;
import br.com.forjacode.taskmanager.domain.exception.TaskNotFoundException;
import br.com.forjacode.taskmanager.domain.model.Task;

import java.util.UUID;

public class CreateTaskService implements CreateTaskUseCase {

    private final TaskRepositoryPort repositoryPort;

    public CreateTaskService(TaskRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public Task execute(CreateTaskCommand command) {
        Task task = Task.create(command.title(), command.description(), command.priority(), command.dueDate());
        repositoryPort.save(task);
        return task;
    }
}
