package br.com.forjacode.taskmanager.adapters.output.security;

import br.com.forjacode.taskmanager.application.ports.output.PasswordHasherPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BCryptPasswordHasherAdapter implements PasswordHasherPort {

    private final BCryptPasswordEncoder encoder;

    public BCryptPasswordHasherAdapter(BCryptPasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public String hash(String rawPassword) {
        log.debug("Hashing password");
        String hashed = encoder.encode(rawPassword);
        log.debug("Password hashed successfully");
        return hashed;
    }

    @Override
    public boolean matches(String rawPassword, String hashedPassword) {
        log.debug("Verifying password match");
        boolean matches = encoder.matches(rawPassword, hashedPassword);
        log.debug("Password match result: {}", matches);
        return matches;
    }
}
