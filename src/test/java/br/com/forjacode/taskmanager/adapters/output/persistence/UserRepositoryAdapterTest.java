package br.com.forjacode.taskmanager.adapters.output.persistence;

import br.com.forjacode.taskmanager.domain.exception.EmailAlreadyInUseException;
import br.com.forjacode.taskmanager.domain.model.User;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRepositoryAdapterTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserRepositoryAdapter userRepositoryAdapter;

    private User user;
    private UserJpaEntity entity;

    @BeforeEach
    void setUp() {
        user = User.create("John Doe", "john.doe@example.com");
        entity = new UserJpaEntity(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should save user successfully when email is not in use")
        void shouldSaveUserSuccessfullyWhenEmailIsNotInUse() {
            when(userMapper.toEntity(user)).thenReturn(entity);

            userRepositoryAdapter.save(user);

            verify(userJpaRepository).saveAndFlush(entity);
        }

        @Test
        @DisplayName("should throw EmailAlreadyInUseException with cause when constraint violation occurs")
        void shouldThrowEmailAlreadyInUseExceptionWhenConstraintViolationOccurs() {
            when(userMapper.toEntity(user)).thenReturn(entity);
            ConstraintViolationException cause = new ConstraintViolationException("duplicate", null, "uk_email");
            doThrow(cause).when(userJpaRepository).saveAndFlush(entity);

            assertThatThrownBy(() -> userRepositoryAdapter.save(user))
                    .isInstanceOf(EmailAlreadyInUseException.class)
                    .hasMessageContaining(user.getEmail())
                    .hasCause(cause);
        }

        @Test
        @DisplayName("should throw EmailAlreadyInUseException with cause when DataIntegrityViolationException occurs")
        void shouldThrowEmailAlreadyInUseExceptionWhenDataIntegrityViolationOccurs() {
            when(userMapper.toEntity(user)).thenReturn(entity);
            DataIntegrityViolationException cause = new DataIntegrityViolationException("duplicate key");
            doThrow(cause).when(userJpaRepository).saveAndFlush(entity);

            assertThatThrownBy(() -> userRepositoryAdapter.save(user))
                    .isInstanceOf(EmailAlreadyInUseException.class)
                    .hasMessageContaining(user.getEmail())
                    .hasCause(cause);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return user when id exists")
        void shouldReturnUserWhenIdExists() {
            when(userJpaRepository.findById(user.getId())).thenReturn(Optional.of(entity));
            when(userMapper.toDomain(entity)).thenReturn(user);

            Optional<User> result = userRepositoryAdapter.findById(user.getId());

            assertThat(result).isPresent().contains(user);
            verify(userJpaRepository).findById(user.getId());
        }

        @Test
        @DisplayName("should return empty when id does not exist")
        void shouldReturnEmptyWhenIdDoesNotExist() {
            UUID id = UUID.randomUUID();
            when(userJpaRepository.findById(id)).thenReturn(Optional.empty());

            Optional<User> result = userRepositoryAdapter.findById(id);

            assertThat(result).isEmpty();
            verify(userJpaRepository).findById(id);
        }
    }

    @Nested
    @DisplayName("findByEmail")
    class FindByEmail {

        @Test
        @DisplayName("should return user when email exists")
        void shouldReturnUserWhenEmailExists() {
            when(userJpaRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(entity));
            when(userMapper.toDomain(entity)).thenReturn(user);

            Optional<User> result = userRepositoryAdapter.findByEmail(user.getEmail());

            assertThat(result).isPresent().contains(user);
            verify(userJpaRepository).findByEmail(user.getEmail());
        }

        @Test
        @DisplayName("should return empty when email does not exist")
        void shouldReturnEmptyWhenEmailDoesNotExist() {
            String email = "missing@example.com";
            when(userJpaRepository.findByEmail(email)).thenReturn(Optional.empty());

            Optional<User> result = userRepositoryAdapter.findByEmail(email);

            assertThat(result).isEmpty();
            verify(userJpaRepository).findByEmail(email);
        }
    }
}

