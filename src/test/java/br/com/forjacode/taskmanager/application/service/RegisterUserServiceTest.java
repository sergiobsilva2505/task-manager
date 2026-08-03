package br.com.forjacode.taskmanager.application.service;

import br.com.forjacode.taskmanager.application.ports.input.command.RegisterUserCommand;
import br.com.forjacode.taskmanager.application.ports.output.PasswordHasherPort;
import br.com.forjacode.taskmanager.application.ports.output.UserRegistrationPort;
import br.com.forjacode.taskmanager.domain.model.AuthIdentity;
import br.com.forjacode.taskmanager.domain.model.User;
import br.com.forjacode.taskmanager.domain.model.enums.AuthProvider;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock
    private UserRegistrationPort userRegistrationPort;

    @Mock
    private PasswordHasherPort passwordHasherPort;

    @InjectMocks
    private RegisterUserService registerUserService;

    @Nested
    @DisplayName("Successful creation")
    class Success {

        @Test
        @DisplayName("should create user and save it when command is valid")
        void shouldCreateUserAndSaveItWhenCommandIsValid() {
            RegisterUserCommand command = new RegisterUserCommand("John Doe", "john.doe@example.com", "rawPassword123");
            String hashedPassword = "$2a$10$hashedPasswordExample";
            when(passwordHasherPort.hash("rawPassword123")).thenReturn(hashedPassword);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            ArgumentCaptor<AuthIdentity> authIdentityCaptor = ArgumentCaptor.forClass(AuthIdentity.class);

            User createdUser = registerUserService.execute(command);

            verify(passwordHasherPort).hash("rawPassword123");
            verify(userRegistrationPort).register(userCaptor.capture(), authIdentityCaptor.capture());

            User savedUser = userCaptor.getValue();
            AuthIdentity savedAuthIdentity = authIdentityCaptor.getValue();

            assertThat(createdUser).isSameAs(savedUser);
            assertThat(createdUser.getId()).isNotNull();
            assertThat(createdUser.getName()).isEqualTo("John Doe");
            assertThat(createdUser.getEmail()).isEqualTo("john.doe@example.com");
            assertThat(createdUser.getCreatedAt()).isNotNull();

            assertThat(savedAuthIdentity.getId()).isNotNull();
            assertThat(savedAuthIdentity.getUserId()).isEqualTo(createdUser.getId());
            assertThat(savedAuthIdentity.getProvider()).isEqualTo(AuthProvider.LOCAL);
            assertThat(savedAuthIdentity.getPasswordHash()).isEqualTo(hashedPassword);
            assertThat(savedAuthIdentity.getProviderUserId()).isNull();
            assertThat(savedAuthIdentity.getCreatedAt()).isNotNull();
        }
    }
}