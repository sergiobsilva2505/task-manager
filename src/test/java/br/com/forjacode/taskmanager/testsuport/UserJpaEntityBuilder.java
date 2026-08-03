package br.com.forjacode.taskmanager.testsuport;

import br.com.forjacode.taskmanager.adapters.output.persistence.UserJpaEntity;

import java.time.Instant;
import java.util.UUID;

public class UserJpaEntityBuilder {
    private UUID id;
    private String name;
    private String email;
    private Instant createdAt;

    public static UserJpaEntity anUser() {
        return new UserJpaEntityBuilder()
                .id(UUID.randomUUID())
                .name("Test User")
                .email("test@example.com")
                .createdAt(Instant.parse("2026-08-01T12:00:00Z"))
                .build();
    }

    public static UserJpaEntity aSecondUser() {
        return new UserJpaEntityBuilder()
                .id(UUID.randomUUID())
                .name("Second User")
                .email("secondUser@example.com")
                .createdAt(Instant.parse("2026-08-01T12:00:00Z"))
                .build();
    }

    public static UserJpaEntity aThirdUser() {
        return new UserJpaEntityBuilder()
                .id(UUID.randomUUID())
                .name("Third User")
                .email("thirdUser@example.com")
                .createdAt(Instant.now())
                .build();
    }

    public UserJpaEntityBuilder id(UUID id) {
        this.id = id;
        return this;
    }

    public UserJpaEntityBuilder name(String name) {
        this.name = name;
        return this;
    }

    public UserJpaEntityBuilder email(String email) {
        this.email = email;
        return this;
    }

    public UserJpaEntityBuilder createdAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public UserJpaEntity build() {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(this.id);
        entity.setName(this.name);
        entity.setEmail(this.email);
        entity.setCreatedAt(this.createdAt);
        return entity;
    }
}
