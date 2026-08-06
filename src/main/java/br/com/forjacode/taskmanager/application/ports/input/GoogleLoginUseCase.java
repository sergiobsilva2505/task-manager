package br.com.forjacode.taskmanager.application.ports.input;

import br.com.forjacode.taskmanager.application.ports.input.command.GoogleLoginCommand;
import br.com.forjacode.taskmanager.application.ports.input.result.LoginResult;

public interface GoogleLoginUseCase {
    LoginResult execute(GoogleLoginCommand command);
}
