package br.com.forjacode.taskmanager.adapters.config;

import br.com.forjacode.taskmanager.application.ports.input.CreateTaskUseCase;
import br.com.forjacode.taskmanager.application.ports.output.TaskRepositoryPort;
import br.com.forjacode.taskmanager.application.service.CreateTaskService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public CreateTaskUseCase createTaskUseCase(TaskRepositoryPort taskRepositoryPort) {
        return new CreateTaskService(taskRepositoryPort);
    }
}
