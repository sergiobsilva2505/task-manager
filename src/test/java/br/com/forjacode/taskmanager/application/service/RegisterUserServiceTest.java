package br.com.forjacode.taskmanager.application.service;

import br.com.forjacode.taskmanager.application.ports.input.command.RegisterUserCommand;
import br.com.forjacode.taskmanager.application.ports.output.UserRepositoryPort;
import br.com.forjacode.taskmanager.domain.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private RegisterUserService registerUserService;

    @Nested
    @DisplayName("Successful creation")
    class Success {

        @Test
        @DisplayName("should create user and save it when command is valid")
        void shouldCreateUserAndSaveItWhenCommandIsValid() {
            RegisterUserCommand command = new RegisterUserCommand("John Doe", "john.doe@example.com");
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            User createdUser = registerUserService.execute(command);

            verify(userRepositoryPort).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();

            assertThat(createdUser).isSameAs(savedUser);
            assertThat(createdUser.getId()).isNotNull();
            assertThat(createdUser.getName()).isEqualTo("John Doe");
            assertThat(createdUser.getEmail()).isEqualTo("john.doe@example.com");
            assertThat(createdUser.getCreatedAt()).isNotNull();
        }
    }
}