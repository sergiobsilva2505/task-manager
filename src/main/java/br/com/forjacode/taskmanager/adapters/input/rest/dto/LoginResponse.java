package br.com.forjacode.taskmanager.adapters.input.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record LoginResponse(
        @Schema(description = "Token JWT emitido para o usuário autenticado")
        String token,
        @Schema(description = "Data e hora de expiração do token")
        Instant expiresAt,
        @Schema(description = "Identificador único do usuário autenticado")
        UUID userId) {}
