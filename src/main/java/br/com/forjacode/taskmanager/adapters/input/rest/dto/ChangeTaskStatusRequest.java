package br.com.forjacode.taskmanager.adapters.input.rest.dto;

import br.com.forjacode.taskmanager.domain.model.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ChangeTaskStatusRequest(
        @Schema(description = "Novo status da tarefa", example = "IN_PROGRESS")
        @NotNull Status status) {
}
