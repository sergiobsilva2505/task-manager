package br.com.forjacode.taskmanager.application.service;

import br.com.forjacode.taskmanager.application.ports.input.GetTaskByIdUseCase;
import br.com.forjacode.taskmanager.application.ports.output.TaskRepositoryPort;
import br.com.forjacode.taskmanager.domain.exception.TaskNotFoundException;
import br.com.forjacode.taskmanager.domain.model.Task;

import java.util.UUID;

public class GetTaskByIdService implements GetTaskByIdUseCase {

    private final TaskRepositoryPort repositoryPort;

    public GetTaskByIdService(TaskRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public Task execute(UUID taskId) {
        return repositoryPort.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task with ID %s not found".formatted(taskId)));
    }
}
