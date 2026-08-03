package br.com.forjacode.taskmanager.application.ports.output;

import java.time.Instant;

public record GeneratedToken(String token, Instant expiresAt) {
}
