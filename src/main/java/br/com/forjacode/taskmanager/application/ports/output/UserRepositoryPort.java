package br.com.forjacode.taskmanager.application.ports.output;

import br.com.forjacode.taskmanager.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {

    void save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);
}
