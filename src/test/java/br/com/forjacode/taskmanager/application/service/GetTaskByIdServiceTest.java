package br.com.forjacode.taskmanager.application.service;

import br.com.forjacode.taskmanager.application.ports.output.TaskRepositoryPort;
import br.com.forjacode.taskmanager.domain.exception.TaskNotFoundException;
import br.com.forjacode.taskmanager.domain.model.Task;
import br.com.forjacode.taskmanager.domain.model.enums.Priority;
import br.com.forjacode.taskmanager.domain.model.enums.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTaskByIdServiceTest {

    @Mock
    private TaskRepositoryPort repositoryPort;

    @InjectMocks
    private GetTaskByIdService getTaskByIdService;

    @Nested
    @DisplayName("Successful retrieval")
    class Success {

        private UUID taskId;
        private Task task;
        private Instant now;
        private LocalDateTime dueDate;
        private UUID ownerId;

        @BeforeEach
        void setUp() {
            taskId = UUID.randomUUID();
            ownerId = UUID.randomUUID();
            now = Instant.parse("2026-08-01T12:00:00Z");
            dueDate = LocalDateTime.of(2026, Month.AUGUST, 1, 12, 0).plusDays(1);

            task = Task.reconstruct(
                taskId,
                "Task title",
                "Task description",
                Status.TODO,
                Priority.HIGH,
                dueDate,
                ownerId,
                now,
                now
            );
        }

        @Test
        @DisplayName("should return task when id exists")
        void shouldReturnTaskWhenIdExists() {
            when(repositoryPort.findById(taskId)).thenReturn(Optional.of(task));

            Task result = getTaskByIdService.execute(taskId, ownerId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(taskId);
            assertThat(result.getTitle()).isEqualTo("Task title");
            assertThat(result.getDescription()).isEqualTo("Task description");
            assertThat(result.getStatus()).isEqualTo(Status.TODO);
            assertThat(result.getPriority()).isEqualTo(Priority.HIGH);
            assertThat(result.getDueDate()).isEqualTo(dueDate);
            verify(repositoryPort).findById(taskId);
        }
    }

    @Nested
    @DisplayName("Retrieval with error")
    class WithError {

        private UUID taskId;
        private UUID ownerId;

        @BeforeEach
        void setUp() {
            taskId = UUID.randomUUID();
            ownerId = UUID.randomUUID();
        }

        @Test
        @DisplayName("should throw exception when task does not exist")
        void shouldThrowExceptionWhenTaskDoesNotExist() {
            when(repositoryPort.findById(taskId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> getTaskByIdService.execute(taskId, ownerId))
                .isInstanceOf(TaskNotFoundException.class);
            verify(repositoryPort).findById(taskId);
        }

        @Test
        @DisplayName("should throw exception when task exists but belongs to another user")
        void shouldThrowExceptionWhenTaskBelongsToAnotherUser() {
            UUID anotherUserId = UUID.randomUUID();
            Instant now = Instant.now();
            LocalDateTime dueDate = LocalDateTime.now().plusDays(1);

            Task task = Task.reconstruct(
                taskId,
                "Task title",
                "Task description",
                Status.TODO,
                Priority.HIGH,
                dueDate,
                anotherUserId, // task pertence a outro usuário
                now,
                now
            );

            when(repositoryPort.findById(taskId)).thenReturn(Optional.of(task));

            assertThatThrownBy(() -> getTaskByIdService.execute(taskId, ownerId))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("Task with ID %s not found for user".formatted(taskId));
            verify(repositoryPort).findById(taskId);
        }
    }
}
