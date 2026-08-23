package br.com.forjacode.taskmanager.adapters.output.persistence;

import br.com.forjacode.taskmanager.application.ports.output.AuthIdentityRepositoryPort;
import br.com.forjacode.taskmanager.application.ports.output.UserRepositoryPort;
import br.com.forjacode.taskmanager.domain.model.AuthIdentity;
import br.com.forjacode.taskmanager.domain.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class UserRegistrationAdapterTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private AuthIdentityRepositoryPort authIdentityRepositoryPort;

    @InjectMocks
    private UserRegistrationAdapter userRegistrationAdapter;

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("should save user before saving auth identity")
        void shouldSaveUserBeforeSavingAuthIdentity() {
            User user = User.create("John Doe", "john.doe@example.com");
            AuthIdentity authIdentity = AuthIdentity.createLocal(UUID.randomUUID(), "hashed-password");

            userRegistrationAdapter.register(user, authIdentity);

            InOrder inOrder = inOrder(userRepositoryPort, authIdentityRepositoryPort);
            inOrder.verify(userRepositoryPort).save(user);
            inOrder.verify(authIdentityRepositoryPort).save(authIdentity);
            verifyNoMoreInteractions(userRepositoryPort, authIdentityRepositoryPort);
        }
    }
}

