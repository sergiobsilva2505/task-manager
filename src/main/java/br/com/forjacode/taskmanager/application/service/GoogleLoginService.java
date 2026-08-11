package br.com.forjacode.taskmanager.application.service;

import br.com.forjacode.taskmanager.application.ports.input.GoogleLoginUseCase;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class GoogleLoginService implements GoogleLoginUseCase {

    private static final Logger log = LoggerFactory.getLogger(GoogleLoginService.class);

    private final GoogleTokenVerifierPort googleTokenVerifierPort;
    private final AuthIdentityRepositoryPort authIdentityRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final UserRegistrationPort userRegistrationPort;
    private final TokenGeneratorPort tokenGeneratorPort;

    public GoogleLoginService(GoogleTokenVerifierPort googleTokenVerifierPort,
            AuthIdentityRepositoryPort authIdentityRepositoryPort, UserRepositoryPort userRepositoryPort,
            UserRegistrationPort userRegistrationPort, TokenGeneratorPort tokenGeneratorPort) {
        this.googleTokenVerifierPort = googleTokenVerifierPort;
        this.authIdentityRepositoryPort = authIdentityRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.userRegistrationPort = userRegistrationPort;
        this.tokenGeneratorPort = tokenGeneratorPort;
    }

    @Override
    public LoginResult execute(GoogleLoginCommand command) {
        GoogleUserInfo googleUserInfo = googleTokenVerifierPort.verify(command.idToken())
                .orElseThrow(() -> {
                    log.warn("Google login failed: token validation failed");
                    return new InvalidGoogleTokenException();
                });

        UUID userId = resolveUserId(googleUserInfo);

        GeneratedToken generatedToken = tokenGeneratorPort.generate(userId);

        log.info("User {} logged in via GOOGLE", userId);

        return new LoginResult(generatedToken.token(), generatedToken.expiresAt(), userId);
    }

    private UUID resolveUserId(GoogleUserInfo googleUserInfo) {
        return authIdentityRepositoryPort
                .findByProviderAndProviderUserId(AuthProvider.GOOGLE, googleUserInfo.googleUserId())
                .map(authIdentity -> {
                    log.debug("Google identity already linked to user {}", authIdentity.getUserId());
                    return authIdentity.getUserId();
                })
                .orElseGet(() -> linkOrCreateUser(googleUserInfo));
    }

    private UUID linkOrCreateUser(GoogleUserInfo googleUserInfo) {
        return userRepositoryPort.findByEmail(googleUserInfo.email())
                .map(existingUser -> linkGoogleIdentity(existingUser, googleUserInfo))
                .orElseGet(() -> createUserWithGoogleIdentity(googleUserInfo));
    }

    private UUID linkGoogleIdentity(User existingUser, GoogleUserInfo googleUserInfo) {
        AuthIdentity authIdentity = AuthIdentity.createOAuth(
                existingUser.getId(), AuthProvider.GOOGLE, googleUserInfo.googleUserId());
        authIdentityRepositoryPort.save(authIdentity);
        log.debug("Linked Google identity to existing user {}", existingUser.getId());
        return existingUser.getId();
    }

    private UUID createUserWithGoogleIdentity(GoogleUserInfo googleUserInfo) {
        String name = (googleUserInfo.name() != null && !googleUserInfo.name().isBlank())
                ? googleUserInfo.name()
                : deriveNameFromEmail(googleUserInfo.email());

        User user = User.create(name, googleUserInfo.email());
        AuthIdentity authIdentity = AuthIdentity.createOAuth(
                user.getId(), AuthProvider.GOOGLE, googleUserInfo.googleUserId());

        userRegistrationPort.register(user, authIdentity);

        log.debug("Created new user {} via Google login", user.getId());
        log.info("User registered: {} via GOOGLE", user.getId());

        return user.getId();
    }

    private String deriveNameFromEmail(String email) {
        String localPart = email.split("@")[0];
        String cleaned = localPart.replaceAll("[^\\p{L}\\s'-]", " ").trim();

        if (cleaned.length() < 3) {
            return "Google User";
        }

        return cleaned;
    }
}