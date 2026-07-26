package br.com.forjacode.taskmanager.application.service;

import br.com.forjacode.taskmanager.application.ports.input.RegisterUserUseCase;
import br.com.forjacode.taskmanager.application.ports.input.command.RegisterUserCommand;
import br.com.forjacode.taskmanager.application.ports.output.UserRepositoryPort;
import br.com.forjacode.taskmanager.domain.model.User;

public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public RegisterUserService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public User execute(RegisterUserCommand command) {
        User user = User.create(command.name(), command.email());
        userRepositoryPort.save(user);
        return user;
    }
}
