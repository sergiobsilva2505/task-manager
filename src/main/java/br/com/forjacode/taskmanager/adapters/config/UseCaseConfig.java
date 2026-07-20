package br.com.forjacode.taskmanager.adapters.config;

import br.com.forjacode.taskmanager.application.ports.input.CreateTaskUseCase;
import br.com.forjacode.taskmanager.application.ports.input.GetTaskByIdUseCase;
import br.com.forjacode.taskmanager.application.ports.input.ListTasksUseCase;
import br.com.forjacode.taskmanager.application.ports.output.TaskRepositoryPort;
import br.com.forjacode.taskmanager.application.service.CreateTaskService;
import br.com.forjacode.taskmanager.application.service.GetTaskByIdService;
import br.com.forjacode.taskmanager.application.service.ListTasksService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public CreateTaskUseCase createTaskUseCase(TaskRepositoryPort taskRepositoryPort) {
        return new CreateTaskService(taskRepositoryPort);
    }

    @Bean
    public GetTaskByIdUseCase getTaskByIdUseCase(TaskRepositoryPort taskRepositoryPort) {
        return new GetTaskByIdService(taskRepositoryPort);
    }

    @Bean
    public ListTasksUseCase listTasksUseCase(TaskRepositoryPort taskRepositoryPort) {
        return new ListTasksService(taskRepositoryPort);
    }
}
