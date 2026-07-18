package br.com.forjacode.taskmanager.adapters.input.rest.dto;

import br.com.forjacode.taskmanager.domain.model.enums.Priority;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateTaskRequest(
        @NotBlank String title,
        String description,
        @NotNull Priority priority,
        @FutureOrPresent LocalDateTime dueDate) {
}
