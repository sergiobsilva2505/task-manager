package br.com.forjacode.taskmanager.application.service;

import br.com.forjacode.taskmanager.application.ports.input.command.GoogleLoginCommand;
import br.com.forjacode.taskmanager.application.ports.input.result.LoginResult;
import br.com.forjacode.taskmanager.application.ports.output.AuthIdentityRepositoryPort;
import br.com.forjacode.taskmanager.application.ports.output.GeneratedToken;
import br.com.forjacode.taskmanager.application.ports.output.GoogleTokenVerifierPort;
import br.com.forjacode.taskmanager.application.ports.output.GoogleUserInfo;
import br.com.forjacode.taskmanager.application.ports.output.TokenGeneratorPort;
import br.com.forjacode.taskmanager.application.ports.output.UserRegistrationPort;
import br.com.forjacode.taskmanager.application.ports.output.UserRepositoryPort;
import br.com.forjacode.taskmanager.domain.exception.InvalidGoogleTokenException;
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

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleLoginServiceTest {

    @Mock
    private GoogleTokenVerifierPort googleTokenVerifierPort;

    @Mock
    private AuthIdentityRepositoryPort authIdentityRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private UserRegistrationPort userRegistrationPort;

    @Mock
    private TokenGeneratorPort tokenGeneratorPort;

    @InjectMocks
    private GoogleLoginService googleLoginService;

    @Nested
    @DisplayName("Success")
    class Success {

        @Test
        @DisplayName("should return token when google identity already exists")
        void shouldReturnTokenWhenGoogleIdentityAlreadyExists() {
            GoogleLoginCommand command = new GoogleLoginCommand("valid-id-token");
            GoogleUserInfo googleUserInfo = new GoogleUserInfo("john.doe@example.com", "John Doe", "google-user-id-123");
            UUID existingUserId = UUID.randomUUID();
            AuthIdentity authIdentity = AuthIdentity.createOAuth(existingUserId, AuthProvider.GOOGLE, "google-user-id-123");
            Instant expiresAt = Instant.now().plusSeconds(3600);
            GeneratedToken generatedToken = new GeneratedToken("jwt-token", expiresAt);

            when(googleTokenVerifierPort.verify("valid-id-token")).thenReturn(Optional.of(googleUserInfo));
            when(authIdentityRepositoryPort.findByProviderAndProviderUserId(AuthProvider.GOOGLE, "google-user-id-123"))
                    .thenReturn(Optional.of(authIdentity));
            when(tokenGeneratorPort.generate(existingUserId)).thenReturn(generatedToken);

            LoginResult result = googleLoginService.execute(command);

            assertThat(result.token()).isEqualTo("jwt-token");
            assertThat(result.expiresAt()).isEqualTo(expiresAt);
            assertThat(result.userId()).isEqualTo(existingUserId);

            verifyNoInteractions(userRepositoryPort, userRegistrationPort);
        }

        @Test
        @DisplayName("should link google identity to existing user found by email")
        void shouldLinkGoogleIdentityToExistingUserFoundByEmail() {
            GoogleLoginCommand command = new GoogleLoginCommand("valid-id-token");
            GoogleUserInfo googleUserInfo = new GoogleUserInfo("john.doe@example.com", "John Doe", "google-user-id-123");
            User existingUser = User.create("John Doe", "john.doe@example.com");
            Instant expiresAt = Instant.now().plusSeconds(3600);
            GeneratedToken generatedToken = new GeneratedToken("jwt-token", expiresAt);

            when(googleTokenVerifierPort.verify("valid-id-token")).thenReturn(Optional.of(googleUserInfo));
            when(authIdentityRepositoryPort.findByProviderAndProviderUserId(AuthProvider.GOOGLE, "google-user-id-123"))
                    .thenReturn(Optional.empty());
            when(userRepositoryPort.findByEmail("john.doe@example.com")).thenReturn(Optional.of(existingUser));
            when(tokenGeneratorPort.generate(existingUser.getId())).thenReturn(generatedToken);

            LoginResult result = googleLoginService.execute(command);

            ArgumentCaptor<AuthIdentity> authIdentityCaptor = ArgumentCaptor.forClass(AuthIdentity.class);
            verify(authIdentityRepositoryPort).save(authIdentityCaptor.capture());

            AuthIdentity savedAuthIdentity = authIdentityCaptor.getValue();
            assertThat(savedAuthIdentity.getUserId()).isEqualTo(existingUser.getId());
            assertThat(savedAuthIdentity.getProvider()).isEqualTo(AuthProvider.GOOGLE);
            assertThat(savedAuthIdentity.getProviderUserId()).isEqualTo("google-user-id-123");
            assertThat(savedAuthIdentity.getPasswordHash()).isNull();

            assertThat(result.token()).isEqualTo("jwt-token");
            assertThat(result.expiresAt()).isEqualTo(expiresAt);
            assertThat(result.userId()).isEqualTo(existingUser.getId());

            verifyNoInteractions(userRegistrationPort);
        }

        @Test
        @DisplayName("should create new user and google identity when no user exists with google account email")
        void shouldCreateNewUserAndGoogleIdentityWhenNoUserExistsWithGoogleAccountEmail() {
            GoogleLoginCommand command = new GoogleLoginCommand("valid-id-token");
            GoogleUserInfo googleUserInfo = new GoogleUserInfo("newuser@example.com", "New User", "google-user-id-123");
            Instant expiresAt = Instant.now().plusSeconds(3600);
            GeneratedToken generatedToken = new GeneratedToken("jwt-token", expiresAt);

            when(googleTokenVerifierPort.verify("valid-id-token")).thenReturn(Optional.of(googleUserInfo));
            when(authIdentityRepositoryPort.findByProviderAndProviderUserId(AuthProvider.GOOGLE, "google-user-id-123"))
                    .thenReturn(Optional.empty());
            when(userRepositoryPort.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
            when(tokenGeneratorPort.generate(any())).thenReturn(generatedToken);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            ArgumentCaptor<AuthIdentity> authIdentityCaptor = ArgumentCaptor.forClass(AuthIdentity.class);

            googleLoginService.execute(command);

            verify(userRegistrationPort).register(userCaptor.capture(), authIdentityCaptor.capture());

            User createdUser = userCaptor.getValue();
            AuthIdentity createdAuthIdentity = authIdentityCaptor.getValue();

            assertThat(createdUser.getName()).isEqualTo("New User");
            assertThat(createdUser.getEmail()).isEqualTo("newuser@example.com");

            assertThat(createdAuthIdentity.getUserId()).isEqualTo(createdUser.getId());
            assertThat(createdAuthIdentity.getProvider()).isEqualTo(AuthProvider.GOOGLE);
            assertThat(createdAuthIdentity.getProviderUserId()).isEqualTo("google-user-id-123");
            assertThat(createdAuthIdentity.getPasswordHash()).isNull();

            verify(tokenGeneratorPort).generate(createdUser.getId());
        }

        @Test
        @DisplayName("should derive name from email local part when google name is null")
        void shouldDeriveNameFromEmailLocalPartWhenGoogleNameIsNull() {
            GoogleLoginCommand command = new GoogleLoginCommand("valid-id-token");
            GoogleUserInfo googleUserInfo = new GoogleUserInfo("maria.silva@example.com", null, "google-user-id-123");

            when(googleTokenVerifierPort.verify("valid-id-token")).thenReturn(Optional.of(googleUserInfo));
            when(authIdentityRepositoryPort.findByProviderAndProviderUserId(AuthProvider.GOOGLE, "google-user-id-123"))
                    .thenReturn(Optional.empty());
            when(userRepositoryPort.findByEmail("maria.silva@example.com")).thenReturn(Optional.empty());
            when(tokenGeneratorPort.generate(any()))
                    .thenReturn(new GeneratedToken("jwt-token", Instant.now().plusSeconds(3600)));

            googleLoginService.execute(command);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRegistrationPort).register(userCaptor.capture(), any());

            assertThat(userCaptor.getValue().getName()).isEqualTo("maria silva");
        }

        @Test
        @DisplayName("should fallback to Google User when derived name from email is too short")
        void shouldFallbackToGoogleUserWhenDerivedNameFromEmailIsTooShort() {
            GoogleLoginCommand command = new GoogleLoginCommand("valid-id-token");
            GoogleUserInfo googleUserInfo = new GoogleUserInfo("jd83@example.com", "   ", "google-user-id-123");

            when(googleTokenVerifierPort.verify("valid-id-token")).thenReturn(Optional.of(googleUserInfo));
            when(authIdentityRepositoryPort.findByProviderAndProviderUserId(AuthProvider.GOOGLE, "google-user-id-123"))
                    .thenReturn(Optional.empty());
            when(userRepositoryPort.findByEmail("jd83@example.com")).thenReturn(Optional.empty());
            when(tokenGeneratorPort.generate(any()))
                    .thenReturn(new GeneratedToken("jwt-token", Instant.now().plusSeconds(3600)));

            googleLoginService.execute(command);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRegistrationPort).register(userCaptor.capture(), any());

            assertThat(userCaptor.getValue().getName()).isEqualTo("Google User");
        }
    }

    @Nested
    @DisplayName("WithError")
    class WithError {

        @Test
        @DisplayName("should throw InvalidGoogleTokenException when google token is invalid")
        void shouldThrowInvalidGoogleTokenExceptionWhenGoogleTokenIsInvalid() {
            GoogleLoginCommand command = new GoogleLoginCommand("invalid-id-token");

            when(googleTokenVerifierPort.verify("invalid-id-token")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> googleLoginService.execute(command))
                    .isInstanceOf(InvalidGoogleTokenException.class)
                    .hasMessage("Invalid or expired Google token");

            verifyNoInteractions(authIdentityRepositoryPort, userRepositoryPort, userRegistrationPort, tokenGeneratorPort);
        }
    }
}