package br.com.forjacode.taskmanager.application.service;

import br.com.forjacode.taskmanager.application.ports.input.LoginUseCase;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginService implements LoginUseCase {

    private static final Logger log = LoggerFactory.getLogger(LoginService.class);

    private final UserRepositoryPort userRepositoryPort;
    private final AuthIdentityRepositoryPort authIdentityRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;
    private final TokenGeneratorPort tokenGeneratorPort;

    public LoginService(UserRepositoryPort userRepositoryPort, AuthIdentityRepositoryPort authIdentityRepositoryPort,
            PasswordHasherPort passwordHasherPort, TokenGeneratorPort tokenGeneratorPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.authIdentityRepositoryPort = authIdentityRepositoryPort;
        this.passwordHasherPort = passwordHasherPort;
        this.tokenGeneratorPort = tokenGeneratorPort;
    }

    @Override
    public LoginResult execute(LoginCommand command) {
        User user = userRepositoryPort.findByEmail(command.email())
                .orElseThrow(() -> {
                    log.warn("Login failed: email not found");
                    return new InvalidCredentialsException();
                });

        AuthIdentity authIdentity = authIdentityRepositoryPort.findByUserIdAndProvider(user.getId(), AuthProvider.LOCAL)
                .orElseThrow(() -> {
                    log.warn("Login failed: no LOCAL identity for user {}", user.getId());
                    return new InvalidCredentialsException();
                });

        boolean passwordMatches = passwordHasherPort.matches(command.password(), authIdentity.getPasswordHash());
        if (!passwordMatches) {
            log.warn("Login failed: incorrect password for user {}", user.getId());
            throw new InvalidCredentialsException();
        }

        GeneratedToken generatedToken = tokenGeneratorPort.generate(user.getId());

        log.info("User {} logged in via LOCAL", user.getId());

        return new LoginResult(generatedToken.token(), generatedToken.expiresAt(), user.getId());
    }
}