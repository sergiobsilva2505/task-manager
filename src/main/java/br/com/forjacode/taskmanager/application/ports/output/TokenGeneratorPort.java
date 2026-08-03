package br.com.forjacode.taskmanager.application.ports.output;

import java.util.UUID;

public interface TokenGeneratorPort {
    GeneratedToken generate(UUID userId);
}
