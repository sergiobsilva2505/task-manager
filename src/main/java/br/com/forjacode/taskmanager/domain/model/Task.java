package br.com.forjacode.taskmanager.domain.model;

import br.com.forjacode.taskmanager.domain.exception.InvalidInputException;
import br.com.forjacode.taskmanager.domain.exception.InvalidStatusTransitionException;
import br.com.forjacode.taskmanager.domain.exception.MissingRequiredFieldException;
import br.com.forjacode.taskmanager.domain.model.enums.Priority;
import br.com.forjacode.taskmanager.domain.model.enums.Status;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@SuppressWarnings({"LombokGetterMayBeUsed", "java:S107"}) // Suppress warning for too many parameters in constructor
public class Task {

    private final UUID id;
    private final String title;
    private final String description;
    private Status status;
    private final Priority priority;
    private final LocalDateTime dueDate;
    private final UUID ownerId;
    private final Instant createdAt;
    private Instant updatedAt;

    private Task(UUID id, String title, String description, Status status, Priority priority, LocalDateTime dueDate,
            UUID ownerId, Instant createdAt, Instant updatedAt) {
        if (title == null || title.isBlank()) throw new MissingRequiredFieldException("Title cannot be null or blank");
        if (priority == null) throw new MissingRequiredFieldException("Priority cannot be null");
        if (dueDate == null) throw new MissingRequiredFieldException("Due date cannot be null");
        if (dueDate.isBefore(createdAt.atZone(ZoneId.systemDefault()).toLocalDateTime())) {
            throw new InvalidInputException("Due date cannot be before creation date");
        }
         if (ownerId == null) throw new MissingRequiredFieldException("Owner ID cannot be null");
        this.id = id;
        this.title = title;
        this.status = status;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Task create(String title, String description, Priority priority, LocalDateTime dueDate, UUID ownerId) {
        Instant now = Instant.now();
        return new Task(UUID.randomUUID(), title, description, Status.TODO, priority, dueDate, ownerId, now, now);
    }

    public static Task reconstruct(UUID id, String title, String description, Status status, Priority priority,
            LocalDateTime dueDate, UUID ownerId, Instant createdAt, Instant updatedAt) {
        return new Task(id, title, description, status, priority, dueDate, ownerId, createdAt, updatedAt);
    }

    public void changeStatus(Status newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(
                    "Cannot change status from %s to %s".formatted(this.status, newStatus));
        }
        this.status = newStatus;
        update();
    }

    private void update() {
        this.updatedAt = Instant.now();
    }

//    Getters and Setters

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {
        return status;
    }

    public Priority getPriority() {
        return priority;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
