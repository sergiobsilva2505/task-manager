package br.com.forjacode.taskmanager.application.ports.input;

import br.com.forjacode.taskmanager.application.ports.input.command.CreateTaskCommand;
import br.com.forjacode.taskmanager.domain.model.Task;

public interface CreateTaskUseCase {

    Task execute(CreateTaskCommand command);
}