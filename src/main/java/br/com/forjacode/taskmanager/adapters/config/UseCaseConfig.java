package br.com.forjacode.taskmanager.adapters.config;

import br.com.forjacode.taskmanager.application.ports.input.ChangeTaskStatusUseCase;
import br.com.forjacode.taskmanager.application.ports.input.CreateTaskUseCase;
import br.com.forjacode.taskmanager.application.ports.input.DeleteTaskUseCase;
import br.com.forjacode.taskmanager.application.ports.input.GetTaskByIdUseCase;
import br.com.forjacode.taskmanager.application.ports.input.GoogleLoginUseCase;
import br.com.forjacode.taskmanager.application.ports.input.ListTasksUseCase;
import br.com.forjacode.taskmanager.application.ports.input.LoginUseCase;
import br.com.forjacode.taskmanager.application.ports.input.RegisterUserUseCase;
import br.com.forjacode.taskmanager.application.ports.output.AuthIdentityRepositoryPort;
import br.com.forjacode.taskmanager.application.ports.output.GoogleTokenVerifierPort;
import br.com.forjacode.taskmanager.application.ports.output.PasswordHasherPort;
import br.com.forjacode.taskmanager.application.ports.output.TaskRepositoryPort;
import br.com.forjacode.taskmanager.application.ports.output.TokenGeneratorPort;
import br.com.forjacode.taskmanager.application.ports.output.UserRegistrationPort;
import br.com.forjacode.taskmanager.application.ports.output.UserRepositoryPort;
import br.com.forjacode.taskmanager.application.service.ChangeTaskStatusService;
import br.com.forjacode.taskmanager.application.service.CreateTaskService;
import br.com.forjacode.taskmanager.application.service.DeleteTaskService;
import br.com.forjacode.taskmanager.application.service.GetTaskByIdService;
import br.com.forjacode.taskmanager.application.service.GoogleLoginService;
import br.com.forjacode.taskmanager.application.service.ListTasksService;
import br.com.forjacode.taskmanager.application.service.LoginService;
import br.com.forjacode.taskmanager.application.service.RegisterUserService;
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

    @Bean
    public ChangeTaskStatusUseCase changeTaskStatusUseCase(TaskRepositoryPort taskRepositoryPort) {
        return new ChangeTaskStatusService(taskRepositoryPort);
    }

    @Bean
    public DeleteTaskUseCase deleteTaskUseCase(TaskRepositoryPort taskRepositoryPort) {
        return new DeleteTaskService(taskRepositoryPort);
    }

    @Bean
    public RegisterUserUseCase registerUserUseCase(UserRegistrationPort userRegistrationPort,
            PasswordHasherPort passwordHasherPort) {
        return new RegisterUserService(userRegistrationPort, passwordHasherPort);
    }

    @Bean
    public LoginUseCase loginUseCase(UserRepositoryPort userRepositoryPort,
            AuthIdentityRepositoryPort authIdentityRepositoryPort, PasswordHasherPort passwordHasherPort,
            TokenGeneratorPort tokenGeneratorPort) {
        return new LoginService(userRepositoryPort, authIdentityRepositoryPort, passwordHasherPort, tokenGeneratorPort);
    }

    @Bean
    public GoogleLoginUseCase googleLoginUseCase(GoogleTokenVerifierPort googleTokenVerifierPort,
            AuthIdentityRepositoryPort authIdentityRepositoryPort, UserRepositoryPort userRepositoryPort,
            UserRegistrationPort userRegistrationPort, TokenGeneratorPort tokenGeneratorPort) {
        return new GoogleLoginService(googleTokenVerifierPort, authIdentityRepositoryPort, userRepositoryPort,
                userRegistrationPort, tokenGeneratorPort);
    }
}