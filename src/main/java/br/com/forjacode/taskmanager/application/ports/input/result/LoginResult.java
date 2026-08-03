package br.com.forjacode.taskmanager.application.ports.input.result;

import java.time.Instant;
import java.util.UUID;

public record LoginResult(String token, Instant expiresAt, UUID userId) {
}
