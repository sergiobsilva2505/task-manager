package br.com.forjacode.taskmanager.application.service;

import br.com.forjacode.taskmanager.application.ports.input.RegisterUserCase;
import br.com.forjacode.taskmanager.application.ports.input.command.CreateUserCommand;
import br.com.forjacode.taskmanager.application.ports.output.UserRepositoryPort;
import br.com.forjacode.taskmanager.domain.model.User;

public class RegisterUserService implements RegisterUserCase {

    private final UserRepositoryPort userRepositoryPort;

    public RegisterUserService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public User execute(CreateUserCommand command) {
        User user = User.create(command.name(), command.email());
        userRepositoryPort.save(user);
        return user;
    }
}
