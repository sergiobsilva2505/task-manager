package br.com.forjacode.taskmanager.adapters.input.rest.dto;

import java.time.Instant;
import java.util.UUID;

public record LoginResponse(String token, Instant expiresAt, UUID userId) {}
