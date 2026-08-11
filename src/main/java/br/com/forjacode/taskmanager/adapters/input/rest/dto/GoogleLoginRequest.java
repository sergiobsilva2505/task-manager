package br.com.forjacode.taskmanager.adapters.input.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
        @Schema(description = "ID Token emitido pelo Google")
        @NotBlank String idToken) {
}
