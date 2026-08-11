package br.com.forjacode.taskmanager.adapters.input.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        @Schema(description = "Identificador único do usuário")
        UUID id,
        @Schema(description = "Nome do usuário", example = "João da Silva")
        String name,
        @Schema(description = "E-mail do usuário", example = "joao.silva@example.com")
        String email,
        @Schema(description = "Data e hora de criação do usuário")
        Instant createdAt) {
}