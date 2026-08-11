package br.com.forjacode.taskmanager.application.service;

import br.com.forjacode.taskmanager.application.ports.input.CreateTaskUseCase;
import br.com.forjacode.taskmanager.application.ports.input.command.CreateTaskCommand;
import br.com.forjacode.taskmanager.application.ports.output.TaskRepositoryPort;
import br.com.forjacode.taskmanager.domain.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateTaskService implements CreateTaskUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateTaskService.class);

    private final TaskRepositoryPort repositoryPort;

    public CreateTaskService(TaskRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public Task execute(CreateTaskCommand command) {
        Task task = Task.create(command.title(), command.description(), command.priority(), command.dueDate(),
                command.ownerId());
        repositoryPort.save(task);

        log.info("Task created: {} for user {}", task.getId(), task.getOwnerId());

        return task;
    }
}