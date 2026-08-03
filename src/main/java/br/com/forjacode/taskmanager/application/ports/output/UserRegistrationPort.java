package br.com.forjacode.taskmanager.application.ports.output;

import br.com.forjacode.taskmanager.domain.model.AuthIdentity;
import br.com.forjacode.taskmanager.domain.model.User;

public interface UserRegistrationPort {
    void register(User user, AuthIdentity authIdentity);
}
