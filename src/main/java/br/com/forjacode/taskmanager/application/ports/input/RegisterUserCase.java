package br.com.forjacode.taskmanager.application.ports.input;

import br.com.forjacode.taskmanager.application.ports.input.command.CreateUserCommand;
import br.com.forjacode.taskmanager.domain.model.User;

public interface RegisterUserCase {

    User execute(CreateUserCommand command);
}
