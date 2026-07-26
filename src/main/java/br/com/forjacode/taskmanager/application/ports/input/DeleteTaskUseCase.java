package br.com.forjacode.taskmanager.application.ports.input;

import java.util.UUID;

public interface DeleteTaskUseCase {

    void execute(UUID taskId, UUID userId);
}
