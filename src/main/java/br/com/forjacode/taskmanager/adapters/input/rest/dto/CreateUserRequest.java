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
        String email,
        @NotBlank
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{8,}$",
                message = "Password must contain at least one uppercase letter, one number, and one special character")
        @Size(min = 8, max = 72)
        String password) {
}
