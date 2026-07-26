package br.com.forjacode.taskmanager.application.ports.input;

import br.com.forjacode.taskmanager.application.ports.input.command.RegisterUserCommand;
import br.com.forjacode.taskmanager.domain.model.User;

public interface RegisterUserUseCase {

    User execute(RegisterUserCommand command);
}
