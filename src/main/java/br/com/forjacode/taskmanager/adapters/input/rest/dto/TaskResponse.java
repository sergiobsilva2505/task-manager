package br.com.forjacode.taskmanager.adapters.input.rest.dto;

import br.com.forjacode.taskmanager.domain.model.enums.Priority;
import br.com.forjacode.taskmanager.domain.model.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record TaskResponse(
        @Schema(description = "Identificador único da tarefa")
        UUID id,
        @Schema(description = "Título da tarefa", example = "Finalizar o projeto")
        String title,
        @Schema(description = "Descrição da tarefa", example = "Concluir todas as tarefas pendentes do projeto")
        String description,
        @Schema(description = "Status atual da tarefa", example = "TODO")
        Status status,
        @Schema(description = "Prioridade da tarefa", example = "HIGH")
        Priority priority,
        @Schema(description = "Prazo de vencimento da tarefa", example = "2024-12-31T23:59:59")
        LocalDateTime dueDate,
        @Schema(description = "Data e hora de criação da tarefa")
        Instant createdAt,
        @Schema(description = "Data e hora da última atualização da tarefa")
        Instant updatedAt) {
}
