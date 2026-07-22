package br.com.forjacode.taskmanager.application.ports.input.command;

import br.com.forjacode.taskmanager.domain.model.enums.Status;

import java.util.UUID;

public record ChangeTaskStatusCommand(UUID taskId, Status newStatus) {
}
