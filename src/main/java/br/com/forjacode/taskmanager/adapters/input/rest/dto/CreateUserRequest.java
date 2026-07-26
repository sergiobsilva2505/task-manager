package br.com.forjacode.taskmanager.adapters.input.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank
        @Size(min = 3, max = 160)
        @Pattern(regexp = "^[\\p{L}\\s'-]+$", message = "Name must contain only letters, spaces, hyphens or apostrophes")
        String name,
        @NotBlank
        @Email
        String email) {
}
