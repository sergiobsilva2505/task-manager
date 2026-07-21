package br.com.forjacode.taskmanager.adapters.input.rest.dto;

import br.com.forjacode.taskmanager.domain.model.enums.Priority;
import br.com.forjacode.taskmanager.domain.model.enums.Status;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record ChangeTaskStatusRequest(
        UUID id,
        String title,
        String description,
        Status status,
        Priority priority,
        LocalDateTime dueDate,
        Instant createdAt,
        Instant updatedAt) {
}
