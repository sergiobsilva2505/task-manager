package br.com.forjacode.taskmanager.application.service;

import br.com.forjacode.taskmanager.application.ports.input.DeleteTaskUseCase;
import br.com.forjacode.taskmanager.application.ports.output.TaskRepositoryPort;

import java.util.UUID;

public class DeleteTaskService implements DeleteTaskUseCase {

    private final TaskRepositoryPort taskRepository;

    public DeleteTaskService(TaskRepositoryPort taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public void execute(UUID taskId, UUID userId) {
        taskRepository.deleteByIdAndOwnerId(taskId, userId);
    }
}
