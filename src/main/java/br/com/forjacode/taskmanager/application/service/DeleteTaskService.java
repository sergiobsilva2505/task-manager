package br.com.forjacode.taskmanager.application.service;

import br.com.forjacode.taskmanager.application.ports.input.DeleteTaskUseCase;
import br.com.forjacode.taskmanager.application.ports.output.TaskRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class DeleteTaskService implements DeleteTaskUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteTaskService.class);

    private final TaskRepositoryPort taskRepository;

    public DeleteTaskService(TaskRepositoryPort taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public void execute(UUID taskId, UUID userId) {
        taskRepository.deleteByIdAndOwnerId(taskId, userId);

        log.info("Task deletion requested: {} by user {}", taskId, userId);
    }
}