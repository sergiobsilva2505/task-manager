package br.com.forjacode.taskmanager.domain.model;

import br.com.forjacode.taskmanager.domain.exception.InvalidInputException;
import br.com.forjacode.taskmanager.domain.exception.MissingRequiredFieldException;

import java.time.Instant;
import java.util.UUID;

@SuppressWarnings("LombokGetterMayBeUsed")
public class User {

    private final UUID id;
    private final String name;
    private final String email;
    private final Instant createdAt;

    private User(UUID id, String name, String email, Instant createdAt) {

        validateRequiredFields(name, email, createdAt);
        validateNameFormat(name);

        this.id = id;
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
    }

    private static void validateRequiredFields(String name, String email, Instant createdAt) {
        if (name == null || name.isBlank()) {
            throw new MissingRequiredFieldException("Name cannot be null or blank");
        }
        if (email == null || email.isBlank()) {
            throw new MissingRequiredFieldException("Email cannot be null or blank");
        }
        if (createdAt == null) {
            throw new MissingRequiredFieldException("CreatedAt cannot be null");
        }
    }

    private static void validateNameFormat(String name) {
        if (name.length() < 3 || name.length() > 160) {
            throw new InvalidInputException("Name must be between 3 and 160 characters");
        }
        if (!name.matches("^[\\p{L}\\s'-]+$")) {
            throw new InvalidInputException("Name must contain only letters, spaces, hyphens or apostrophes");
        }
    }

    public static User create(String name, String email) {
        return new User(UUID.randomUUID(), name, email, Instant.now());
    }

    public static User reconstruct(UUID id, String name, String email, Instant createdAt) {
        return new User(id, name, email, createdAt);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}