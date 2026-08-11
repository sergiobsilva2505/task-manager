package br.com.forjacode.taskmanager.adapters.input.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(description = "E-mail do usuário", example = "joao.silva@example.com")
        @NotBlank @Email String email,
        @Schema(description = "Senha do usuário", example = "Senha@123")
        @NotBlank String password) {}
