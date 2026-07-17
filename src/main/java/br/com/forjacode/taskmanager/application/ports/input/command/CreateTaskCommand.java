package br.com.forjacode.taskmanager.application.ports.input.command;

import br.com.forjacode.taskmanager.domain.model.enums.Priority;

import java.time.LocalDateTime;

public record CreateTaskCommand(
        String title,
        String description,
        Priority priority,
        LocalDateTime dueDate) {
}
