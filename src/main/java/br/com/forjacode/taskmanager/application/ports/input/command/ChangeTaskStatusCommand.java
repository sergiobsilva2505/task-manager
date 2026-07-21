package br.com.forjacode.taskmanager.application.ports.input.command;

import java.util.UUID;

public record ChangeTaskStatusCommand(UUID taskId, String newStatus) {
}
