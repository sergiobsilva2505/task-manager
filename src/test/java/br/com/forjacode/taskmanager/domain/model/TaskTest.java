package br.com.forjacode.taskmanager.domain.model;

import br.com.forjacode.taskmanager.domain.exception.InvalidInputException;
import br.com.forjacode.taskmanager.domain.exception.InvalidStatusTransitionException;
import br.com.forjacode.taskmanager.domain.exception.MissingRequiredFieldException;
import br.com.forjacode.taskmanager.domain.model.enums.Priority;
import br.com.forjacode.taskmanager.domain.model.enums.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class TaskTest {

    @Nested
    @DisplayName("Successful creation")
    class Success {

        @Test
        @DisplayName("should create task with valid data")
        void shouldCreateTaskWithValidData() {
            LocalDateTime dueDate = LocalDateTime.now().plusDays(1);
            UUID ownerId = UUID.randomUUID();

            Task task = Task.create("Pay bills", "Monthly bills", Priority.HIGH, dueDate, ownerId);

            assertThat(task.getId()).isNotNull();
            assertThat(task.getTitle()).isEqualTo("Pay bills");
            assertThat(task.getDescription()).isEqualTo("Monthly bills");
            assertThat(task.getPriority()).isEqualTo(Priority.HIGH);
            assertThat(task.getStatus()).isEqualTo(Status.TODO);
            assertThat(task.getDueDate()).isEqualTo(dueDate);
            assertThat(task.getOwnerId()).isEqualTo(ownerId);
            assertThat(task.getCreatedAt()).isNotNull();
            assertThat(task.getUpdatedAt()).isNotNull();
            assertThat(task.getUpdatedAt()).isEqualTo(task.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("Creation with error")
    class WithError {

        @Test
        @DisplayName("should throw exception when title is null")
        void shouldThrowExceptionWhenTitleIsNull() {
            LocalDateTime dueDate = LocalDateTime.now().plusDays(1);
            UUID ownerId = UUID.randomUUID();

            assertThatThrownBy(() -> Task.create(null, "description", Priority.MEDIUM, dueDate, ownerId))
                    .isInstanceOf(MissingRequiredFieldException.class)
                    .hasMessage("Title cannot be null or blank");
        }

        @Test
        @DisplayName("should throw exception when title is blank")
        void shouldThrowExceptionWhenTitleIsBlank() {
            LocalDateTime dueDate = LocalDateTime.now().plusDays(1);
            UUID ownerId = UUID.randomUUID();

            assertThatThrownBy(() -> Task.create("   ", "description", Priority.MEDIUM, dueDate, ownerId))
                    .isInstanceOf(MissingRequiredFieldException.class)
                    .hasMessage("Title cannot be null or blank");
        }

        @Test
        @DisplayName("should throw exception when priority is null")
        void shouldThrowExceptionWhenPriorityIsNull() {
            LocalDateTime dueDate = LocalDateTime.now().plusDays(1);
            UUID ownerId = UUID.randomUUID();

            assertThatThrownBy(() -> Task.create("Task", "description", null, dueDate, ownerId))
                    .isInstanceOf(MissingRequiredFieldException.class)
                    .hasMessage("Priority cannot be null");
        }

        @Test
        @DisplayName("should throw exception when due date is null")
        void shouldThrowExceptionWhenDueDateIsNull() {
            UUID ownerId = UUID.randomUUID();

            assertThatThrownBy(() -> Task.create("Task", "description", Priority.MEDIUM, null, ownerId))
                    .isInstanceOf(MissingRequiredFieldException.class)
                    .hasMessage("Due date cannot be null");
        }

        @Test
        @DisplayName("should throw exception when due date is before creation date")
        void shouldThrowExceptionWhenDueDateIsBeforeCreationDate() {
            UUID id = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            Instant createdAt = Instant.parse("2026-07-18T10:00:00Z");
            LocalDateTime dueDate = createdAt.atZone(ZoneId.systemDefault()).toLocalDateTime().minusMinutes(1);

            assertThatThrownBy(() -> Task.reconstruct(
                    id,
                    "Task",
                    "description",
                    Status.TODO,
                    Priority.MEDIUM,
                    dueDate,
                    ownerId,
                    createdAt,
                    createdAt))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("Due date cannot be before creation date");
        }

        @Test
        @DisplayName("should throw exception when reconstructing task with null title")
        void shouldThrowExceptionWhenReconstructingTaskWithNullTitle() {
            UUID id = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            Instant createdAt = Instant.parse("2026-07-18T10:00:00Z");
            LocalDateTime dueDate = createdAt.atZone(ZoneId.systemDefault()).toLocalDateTime().plusHours(1);

            assertThatThrownBy(() -> Task.reconstruct(
                    id,
                    null,
                    "description",
                    Status.TODO,
                    Priority.MEDIUM,
                    dueDate,
                    ownerId,
                    createdAt,
                    createdAt))
                    .isInstanceOf(MissingRequiredFieldException.class)
                    .hasMessage("Title cannot be null or blank");
        }

        @Test
        @DisplayName("should throw exception when reconstructing task with null priority")
        void shouldThrowExceptionWhenReconstructingTaskWithNullPriority() {
            UUID id = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            Instant createdAt = Instant.parse("2026-07-18T10:00:00Z");
            LocalDateTime dueDate = createdAt.atZone(ZoneId.systemDefault()).toLocalDateTime().plusHours(1);

            assertThatThrownBy(() -> Task.reconstruct(
                    id,
                    "Task",
                    "description",
                    Status.TODO,
                    null,
                    dueDate,
                    ownerId,
                    createdAt,
                    createdAt))
                    .isInstanceOf(MissingRequiredFieldException.class)
                    .hasMessage("Priority cannot be null");
        }
    }

    @Nested
    @DisplayName("Status transitions")
    class StatusTransition {

        @Test
        @DisplayName("should change status from TODO to IN_PROGRESS")
        void shouldChangeStatusFromTodoToInProgress() {
            UUID ownerId = UUID.randomUUID();
            Task task = Task.create("Task", "description", Priority.LOW, LocalDateTime.now().plusDays(1), ownerId);
            Instant previousUpdatedAt = task.getUpdatedAt();

            task.changeStatus(Status.IN_PROGRESS);

            assertThat(task.getStatus()).isEqualTo(Status.IN_PROGRESS);
            assertThat(task.getUpdatedAt()).isAfterOrEqualTo(previousUpdatedAt);
        }

        @Test
        @DisplayName("should change status from IN_PROGRESS to DONE")
        void shouldChangeStatusFromInProgressToDone() {
            UUID ownerId = UUID.randomUUID();
            Task task = Task.create("Task", "description", Priority.LOW, LocalDateTime.now().plusDays(1), ownerId);
            task.changeStatus(Status.IN_PROGRESS);

            task.changeStatus(Status.DONE);

            assertThat(task.getStatus()).isEqualTo(Status.DONE);
        }

        @Test
        @DisplayName("should throw exception when changing status from TODO to DONE")
        void shouldThrowExceptionWhenChangingStatusFromTodoToDone() {
            UUID ownerId = UUID.randomUUID();
            Task task = Task.create("Task", "description", Priority.LOW, LocalDateTime.now().plusDays(1), ownerId);

            assertThatThrownBy(() -> task.changeStatus(Status.DONE))
                    .isInstanceOf(InvalidStatusTransitionException.class)
                    .hasMessage("Cannot change status from TODO to DONE");
        }

        @Test
        @DisplayName("should throw exception when changing status to same status")
        void shouldThrowExceptionWhenChangingStatusToSameStatus() {
            UUID ownerId = UUID.randomUUID();
            Task task = Task.create("Task", "description", Priority.LOW, LocalDateTime.now().plusDays(1), ownerId);

            assertThatThrownBy(() -> task.changeStatus(Status.TODO))
                    .isInstanceOf(InvalidStatusTransitionException.class)
                    .hasMessage("Cannot change status from TODO to TODO");
        }

        @Test
        @DisplayName("should change status from TODO to CANCELLED")
        void shouldChangeStatusFromTodoToCancelled() {
            UUID ownerId = UUID.randomUUID();
            Task task = Task.create("Task", "description", Priority.LOW, LocalDateTime.now().plusDays(1), ownerId);

            task.changeStatus(Status.CANCELLED);

            assertThat(task.getStatus()).isEqualTo(Status.CANCELLED);
        }

        @Test
        @DisplayName("should change status from IN_PROGRESS to CANCELLED")
        void shouldChangeStatusFromInProgressToCancelled() {
            UUID ownerId = UUID.randomUUID();
            Task task = Task.create("Task", "description", Priority.LOW, LocalDateTime.now().plusDays(1), ownerId);
            task.changeStatus(Status.IN_PROGRESS);

            task.changeStatus(Status.CANCELLED);

            assertThat(task.getStatus()).isEqualTo(Status.CANCELLED);
        }

        @Test
        @DisplayName("should throw exception when changing status from DONE to CANCELLED")
        void shouldThrowExceptionWhenChangingStatusFromDoneToCancelled() {
            UUID ownerId = UUID.randomUUID();
            Task task = Task.create("Task", "description", Priority.LOW, LocalDateTime.now().plusDays(1), ownerId);
            task.changeStatus(Status.IN_PROGRESS);
            task.changeStatus(Status.DONE);

            assertThatThrownBy(() -> task.changeStatus(Status.CANCELLED))
                    .isInstanceOf(InvalidStatusTransitionException.class)
                    .hasMessage("Cannot change status from DONE to CANCELLED");
        }

        @Test
        @DisplayName("should throw exception when changing status from CANCELLED to DONE")
        void shouldThrowExceptionWhenChangingStatusFromCancelledToDone() {
            UUID ownerId = UUID.randomUUID();
            Task task = Task.create("Task", "description", Priority.LOW, LocalDateTime.now().plusDays(1), ownerId);
            task.changeStatus(Status.CANCELLED);

            assertThatThrownBy(() -> task.changeStatus(Status.DONE))
                    .isInstanceOf(InvalidStatusTransitionException.class)
                    .hasMessage("Cannot change status from CANCELLED to DONE");
        }

        @Test
        @DisplayName("should throw exception when changing status from IN_PROGRESS to TODO")
        void shouldThrowExceptionWhenChangingStatusFromInProgressToTodo() {
            UUID ownerId = UUID.randomUUID();
            Task task = Task.create("Task", "description", Priority.LOW, LocalDateTime.now().plusDays(1), ownerId);
            task.changeStatus(Status.IN_PROGRESS);

            assertThatThrownBy(() -> task.changeStatus(Status.TODO))
                    .isInstanceOf(InvalidStatusTransitionException.class)
                    .hasMessage("Cannot change status from IN_PROGRESS to TODO");
        }
    }

    @Nested
    @DisplayName("Reconstruction")
    class Reconstruction {

        @Test
        @DisplayName("should reconstruct task with persisted data")
        void shouldReconstructTaskWithPersistedData() {
            UUID id = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();
            Instant createdAt = Instant.parse("2026-07-18T10:00:00Z");
            Instant updatedAt = Instant.parse("2026-07-18T12:00:00Z");
            LocalDateTime dueDate = createdAt.atZone(ZoneId.systemDefault()).toLocalDateTime().plusDays(2);

            Task task = Task.reconstruct(id, "Task", "description", Status.IN_PROGRESS, Priority.HIGH, dueDate, ownerId, createdAt, updatedAt);

            assertThat(task.getId()).isEqualTo(id);
            assertThat(task.getTitle()).isEqualTo("Task");
            assertThat(task.getDescription()).isEqualTo("description");
            assertThat(task.getStatus()).isEqualTo(Status.IN_PROGRESS);
            assertThat(task.getPriority()).isEqualTo(Priority.HIGH);
            assertThat(task.getDueDate()).isEqualTo(dueDate);
            assertThat(task.getOwnerId()).isEqualTo(ownerId);
            assertThat(task.getCreatedAt()).isEqualTo(createdAt);
            assertThat(task.getUpdatedAt()).isEqualTo(updatedAt);
        }
    }
}