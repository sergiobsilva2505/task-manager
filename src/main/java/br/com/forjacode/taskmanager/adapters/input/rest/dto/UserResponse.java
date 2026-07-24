package br.com.forjacode.taskmanager.adapters.input.rest.dto;

import java.util.UUID;

public record UserResponse(UUID id, String name, String email) {
}