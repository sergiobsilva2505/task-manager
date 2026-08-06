package br.com.forjacode.taskmanager.adapters.input.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(@NotBlank String idToken) {
}
