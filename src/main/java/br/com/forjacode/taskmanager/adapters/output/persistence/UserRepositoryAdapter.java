package br.com.forjacode.taskmanager.adapters.output.persistence;

import br.com.forjacode.taskmanager.application.ports.output.UserRepositoryPort;
import br.com.forjacode.taskmanager.domain.exception.EmailAlreadyInUseException;
import br.com.forjacode.taskmanager.domain.model.User;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepositoryAdapter implements UserRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(UserRepositoryAdapter.class);

    private final UserJpaRepository userJpaRepository;
    private final UserMapper userMapper;

    public UserRepositoryAdapter(UserJpaRepository userJpaRepository, UserMapper userMapper) {
        this.userJpaRepository = userJpaRepository;
        this.userMapper = userMapper;
    }

    @Override
    public void save(User user) {
        log.info("Saving user with email {}", user.getEmail());
        try {
            // saveAndFlush forces the INSERT to execute now, inside this try block,
            // instead of being deferred to transaction commit time (where this
            // catch would never trigger).
            userJpaRepository.saveAndFlush(userMapper.toEntity(user));
            log.info("User with email {} saved successfully", user.getEmail());
        } catch (ConstraintViolationException | DataIntegrityViolationException e) {
            log.error("Failed to save user due to constraint violation: email={}", user.getEmail(), e);
            throw new EmailAlreadyInUseException("Email %s is already in use".formatted(user.getEmail()), e);
        }
    }

    @Override
    public Optional<User> findById(UUID id) {
        log.debug("Finding user with id {}", id);
        Optional<User> user = userJpaRepository.findById(id).map(userMapper::toDomain);
        log.debug("Finished finding user with id {}, found={}", id, user.isPresent());
        return user;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        log.debug("Finding user with email {}", email);
        Optional<User> user = userJpaRepository.findByEmail(email).map(userMapper::toDomain);
        log.debug("Finished finding user with email {}, found={}", email, user.isPresent());
        return user;
    }
}
