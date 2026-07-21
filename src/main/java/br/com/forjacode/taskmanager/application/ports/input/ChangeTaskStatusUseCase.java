package br.com.forjacode.taskmanager.application.ports.input;

import br.com.forjacode.taskmanager.application.ports.input.command.ChangeTaskStatusCommand;
import br.com.forjacode.taskmanager.domain.model.Task;

public interface ChangeTaskStatusUseCase {

    Task execute(ChangeTaskStatusCommand command);
}
