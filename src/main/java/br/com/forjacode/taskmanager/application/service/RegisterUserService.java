package br.com.forjacode.taskmanager.application.service;

import br.com.forjacode.taskmanager.application.ports.input.RegisterUserUseCase;
import br.com.forjacode.taskmanager.application.ports.input.command.RegisterUserCommand;
import br.com.forjacode.taskmanager.application.ports.output.PasswordHasherPort;
import br.com.forjacode.taskmanager.application.ports.output.UserRegistrationPort;
import br.com.forjacode.taskmanager.domain.model.AuthIdentity;
import br.com.forjacode.taskmanager.domain.model.User;

public class RegisterUserService implements RegisterUserUseCase {

    private final UserRegistrationPort userRegistrationPort;
    private final PasswordHasherPort passwordHasherPort;

    public RegisterUserService(UserRegistrationPort userRegistrationPort, PasswordHasherPort passwordHasherPort) {
        this.userRegistrationPort = userRegistrationPort;
        this.passwordHasherPort = passwordHasherPort;
    }

    @Override
    public User execute(RegisterUserCommand command) {
        User user = User.create(command.name(), command.email());
        String hashedPassword = passwordHasherPort.hash(command.password());
        AuthIdentity authIdentity = AuthIdentity.createLocal(user.getId(), hashedPassword);

        userRegistrationPort.register(user, authIdentity);

        return user;
    }
}