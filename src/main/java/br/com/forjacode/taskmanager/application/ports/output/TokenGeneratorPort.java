package br.com.forjacode.taskmanager.application.ports.output;

import java.util.Optional;
import java.util.UUID;

public interface TokenGeneratorPort {
    
    GeneratedToken generate(UUID userId);

    Optional<UUID> validate(String token);
}
