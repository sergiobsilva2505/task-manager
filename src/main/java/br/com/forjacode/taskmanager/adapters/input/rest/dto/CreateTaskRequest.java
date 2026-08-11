package br.com.forjacode.taskmanager.adapters.input.rest.dto;

import br.com.forjacode.taskmanager.domain.model.enums.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateTaskRequest(
        @Schema(description = "Título da tarefa", example = "Finalizar o projeto")
        @NotBlank String title,
        @Schema(description = "Descrição da tarefa", example = "Concluir todas as tarefas pendentes do projeto")
        String description,
        @Schema(description = "Prioridade da tarefa", example = "HIGH")
        @NotNull Priority priority,
        @Schema(description = "Prazo de vencimento da tarefa", example = "2024-12-31T23:59:59")
        @FutureOrPresent LocalDateTime dueDate) {
}
