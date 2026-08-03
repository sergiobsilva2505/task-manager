package br.com.forjacode.taskmanager.application.service;

import br.com.forjacode.taskmanager.application.ports.input.command.LoginCommand;
import br.com.forjacode.taskmanager.application.ports.input.result.LoginResult;
import br.com.forjacode.taskmanager.application.ports.output.AuthIdentityRepositoryPort;
import br.com.forjacode.taskmanager.application.ports.output.GeneratedToken;
import br.com.forjacode.taskmanager.application.ports.output.PasswordHasherPort;
import br.com.forjacode.taskmanager.application.ports.output.TokenGeneratorPort;
import br.com.forjacode.taskmanager.application.ports.output.UserRepositoryPort;
import br.com.forjacode.taskmanager.domain.exception.InvalidCredentialsException;
import br.com.forjacode.taskmanager.domain.model.AuthIdentity;
import br.com.forjacode.taskmanager.domain.model.User;
import br.com.forjacode.taskmanager.domain.model.enums.AuthProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private AuthIdentityRepositoryPort authIdentityRepositoryPort;

    @Mock
    private PasswordHasherPort passwordHasherPort;

    @Mock
    private TokenGeneratorPort tokenGeneratorPort;

    @InjectMocks
    private LoginService loginService;

    @Nested
    @DisplayName("Success")
    class Success {

        @Test
        @DisplayName("should return token when credentials are valid")
        void shouldReturnTokenWhenCredentialsAreValid() {
            User user = User.create("John Doe", "john.doe@example.com");
            AuthIdentity authIdentity = AuthIdentity.createLocal(user.getId(), "hashedPassword");
            Instant expiresAt = Instant.now().plusSeconds(3600);
            GeneratedToken generatedToken = new GeneratedToken("jwt-token", expiresAt);

            LoginCommand command = new LoginCommand("john.doe@example.com", "rawPassword123");

            when(userRepositoryPort.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));
            when(authIdentityRepositoryPort.findByUserIdAndProvider(user.getId(), AuthProvider.LOCAL))
                    .thenReturn(Optional.of(authIdentity));
            when(passwordHasherPort.matches("rawPassword123", "hashedPassword")).thenReturn(true);
            when(tokenGeneratorPort.generate(user.getId())).thenReturn(generatedToken);

            LoginResult result = loginService.execute(command);

            assertThat(result.token()).isEqualTo("jwt-token");
            assertThat(result.expiresAt()).isEqualTo(expiresAt);
            assertThat(result.userId()).isEqualTo(user.getId());
        }
    }

    @Nested
    @DisplayName("WithError")
    class WithError {

        @Test
        @DisplayName("should throw InvalidCredentialsException when user does not exist")
        void shouldThrowInvalidCredentialsExceptionWhenUserDoesNotExist() {
            LoginCommand command = new LoginCommand("unknown@example.com", "rawPassword123");

            when(userRepositoryPort.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> loginService.execute(command))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Invalid email or password");

            verifyNoInteractions(authIdentityRepositoryPort, passwordHasherPort, tokenGeneratorPort);
        }

        @Test
        @DisplayName("should throw InvalidCredentialsException when auth identity is missing")
        void shouldThrowInvalidCredentialsExceptionWhenAuthIdentityIsMissing() {
            User user = User.create("John Doe", "john.doe@example.com");
            LoginCommand command = new LoginCommand("john.doe@example.com", "rawPassword123");

            when(userRepositoryPort.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));
            when(authIdentityRepositoryPort.findByUserIdAndProvider(user.getId(), AuthProvider.LOCAL))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> loginService.execute(command))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Invalid email or password");

            verifyNoInteractions(passwordHasherPort, tokenGeneratorPort);
        }

        @Test
        @DisplayName("should throw InvalidCredentialsException when password does not match")
        void shouldThrowInvalidCredentialsExceptionWhenPasswordDoesNotMatch() {
            User user = User.create("John Doe", "john.doe@example.com");
            AuthIdentity authIdentity = AuthIdentity.createLocal(user.getId(), "hashedPassword");
            LoginCommand command = new LoginCommand("john.doe@example.com", "wrongPassword");

            when(userRepositoryPort.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));
            when(authIdentityRepositoryPort.findByUserIdAndProvider(user.getId(), AuthProvider.LOCAL))
                    .thenReturn(Optional.of(authIdentity));
            when(passwordHasherPort.matches("wrongPassword", "hashedPassword")).thenReturn(false);

            assertThatThrownBy(() -> loginService.execute(command))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Invalid email or password");

            verifyNoInteractions(tokenGeneratorPort);
        }
    }
}