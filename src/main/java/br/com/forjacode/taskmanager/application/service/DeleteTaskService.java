package br.com.forjacode.taskmanager.application.service;

import br.com.forjacode.taskmanager.application.ports.input.DeleteTaskUseCase;
import br.com.forjacode.taskmanager.application.ports.output.TaskRepositoryPort;
import br.com.forjacode.taskmanager.domain.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

public class DeleteTaskService implements DeleteTaskUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteTaskService.class);

    private final TaskRepositoryPort taskRepository;

    public DeleteTaskService(TaskRepositoryPort taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public void execute(UUID taskId, UUID userId) {
        Optional<Task> task = taskRepository.findById(taskId)
                .filter(t -> t.getOwnerId().equals(userId));

        if (task.isEmpty()) {
            log.debug("Task {} not found for user {}, nothing to delete", taskId, userId);
            return;
        }

        log.info("Deleting task {} requested by user {}", taskId, userId);
        taskRepository.deleteByIdAndOwnerId(taskId, userId);
        log.info("Task {} deleted for user {}", taskId, userId);
    }
}