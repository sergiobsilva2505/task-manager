package br.com.forjacode.taskmanager.application.ports.input;

import br.com.forjacode.taskmanager.application.ports.input.command.LoginCommand;
import br.com.forjacode.taskmanager.application.ports.input.result.LoginResult;

public interface LoginUseCase {
    LoginResult execute(LoginCommand command);
}
