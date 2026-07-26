package br.com.forjacode.taskmanager.application.ports.input;

import br.com.forjacode.taskmanager.domain.model.Task;

import java.util.UUID;

public interface GetTaskByIdUseCase {

    Task execute(UUID taskId, UUID userId);
}
