package br.com.forjacode.taskmanager.adapters.output.persistence;

import br.com.forjacode.taskmanager.application.ports.output.AuthIdentityRepositoryPort;
import br.com.forjacode.taskmanager.application.ports.output.UserRegistrationPort;
import br.com.forjacode.taskmanager.application.ports.output.UserRepositoryPort;
import br.com.forjacode.taskmanager.domain.model.AuthIdentity;
import br.com.forjacode.taskmanager.domain.model.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserRegistrationAdapter implements UserRegistrationPort {

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
        userRepositoryPort.save(user);
        authIdentityRepositoryPort.save(authIdentity);
    }
}
