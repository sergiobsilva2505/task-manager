package br.com.forjacode.taskmanager.adapters.output.security;

import br.com.forjacode.taskmanager.application.ports.output.GeneratedToken;
import br.com.forjacode.taskmanager.application.ports.output.TokenGeneratorPort;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
@EnableConfigurationProperties(JwtProperties.class)
public class JwtTokenGeneratorAdapter implements TokenGeneratorPort {

    private final SecretKey key;
    private final long expirationMinutes;

    public JwtTokenGeneratorAdapter(JwtProperties properties) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
        this.expirationMinutes = properties.expirationMinutes();
    }

    @Override
    public GeneratedToken generate(UUID userId) {
        log.debug("Generating JWT token for user {}", userId);
        Instant now = Instant.now();
        Instant expiresAt = now.plus(expirationMinutes, ChronoUnit.MINUTES);

        String token = Jwts.builder()
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();

        log.debug("JWT token generated for user {}, expires at {}", userId, expiresAt);

        return new GeneratedToken(token, expiresAt);
    }

    @Override
    public Optional<UUID> validate(String token) {
        log.debug("Validating JWT token");
        try {
            String subject = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();

            log.debug("JWT token validated successfully for user {}", subject);

            return Optional.of(UUID.fromString(subject));
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT token validation failed: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
