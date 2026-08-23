package br.com.forjacode.taskmanager.adapters.output.persistence;

import br.com.forjacode.taskmanager.application.ports.output.AuthIdentityRepositoryPort;
import br.com.forjacode.taskmanager.application.ports.output.UserRegistrationPort;
import br.com.forjacode.taskmanager.application.ports.output.UserRepositoryPort;
import br.com.forjacode.taskmanager.domain.model.AuthIdentity;
import br.com.forjacode.taskmanager.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserRegistrationAdapter implements UserRegistrationPort {

    private static final Logger log = LoggerFactory.getLogger(UserRegistrationAdapter.class);

    private final UserRepositoryPort userRepositoryPort;
    private final AuthIdentityRepositoryPort authIdentityRepositoryPort;

    public UserRegistrationAdapter(UserRepositoryPort userRepositoryPort,
            AuthIdentityRepositoryPort authIdentityRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.authIdentityRepositoryPort = authIdentityRepositoryPort;
    }

    @Override
    @Transactional
    public void register(User user, AuthIdentity authIdentity) {
        log.info("Registering user with email {}", user.getEmail());
        userRepositoryPort.save(user);
        authIdentityRepositoryPort.save(authIdentity);
        log.info("User with email {} registered successfully", user.getEmail());
    }
}
