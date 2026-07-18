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

public class Task {

    private UUID id;
    private String title;
    private String description;
    private Status status;
    private Priority priority;
    private LocalDateTime dueDate;
    private Instant createdAt;
    private Instant updatedAt;

    private Task(UUID id, String title, String description, Status status, Priority priority, LocalDateTime dueDate,
            Instant createdAt, Instant updatedAt) {
        if (title == null || title.isBlank()) throw new MissingRequiredFieldException("Title cannot be null or blank");
        if (priority == null) throw new MissingRequiredFieldException("Priority cannot be null");
        if (dueDate == null) throw new MissingRequiredFieldException("Due date cannot be null");
        if (dueDate.isBefore(createdAt.atZone(ZoneId.systemDefault()).toLocalDateTime())) {
            throw new InvalidInputException("Due date cannot be before creation date");
        }
        this.id = id;
        this.title = title;
        this.status = status;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Task create(String title, String description, Priority priority, LocalDateTime dueDate) {
        Instant now = Instant.now();
        return new Task(UUID.randomUUID(), title, description, Status.TODO, priority, dueDate, now, now);
    }

    public static Task reconstruct(UUID id, String title, String description, Status status, Priority priority,
            LocalDateTime dueDate, Instant createdAt, Instant updatedAt) {
        return new Task(id, title, description, status, priority, dueDate, createdAt, updatedAt);
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
