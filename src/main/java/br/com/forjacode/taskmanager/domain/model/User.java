package br.com.forjacode.taskmanager.domain.model;

import br.com.forjacode.taskmanager.domain.exception.MissingRequiredFieldException;

import java.util.UUID;

@SuppressWarnings("LombokGetterMayBeUsed")
public class User {

    private UUID id;
    private String name;
    private String email;

    private User(UUID id, String name, String email) {
        if (name == null || name.isBlank()) throw new MissingRequiredFieldException("Name cannot be null or blank");
        if (email == null || email.isBlank()) throw new MissingRequiredFieldException("Email cannot be null or blank");
        this.name = name;
        this.email = email;
        this.id = id;
    }

    public static User create(String name, String email) {
        return new User(UUID.randomUUID(), name, email);
    }

    public static User reconstruct(UUID id, String name, String email) {
        return new User(id, name, email);
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
}
