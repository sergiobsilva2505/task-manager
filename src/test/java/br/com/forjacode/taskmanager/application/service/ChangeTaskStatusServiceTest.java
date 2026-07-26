package br.com.forjacode.taskmanager.application.service;

import br.com.forjacode.taskmanager.application.ports.input.command.ChangeTaskStatusCommand;
import br.com.forjacode.taskmanager.application.ports.output.TaskRepositoryPort;
import br.com.forjacode.taskmanager.domain.exception.InvalidStatusTransitionException;
import br.com.forjacode.taskmanager.domain.exception.TaskNotFoundException;
import br.com.forjacode.taskmanager.domain.model.Task;
import br.com.forjacode.taskmanager.domain.model.enums.Priority;
import br.com.forjacode.taskmanager.domain.model.enums.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;

@ExtendWith(MockitoExtension.class)
class ChangeTaskStatusServiceTest {

    @Mock
    private TaskRepositoryPort repositoryPort;

    @InjectMocks
    private ChangeTaskStatusService changeTaskStatusService;

    private UUID ownerId;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("Successful status change")
    class Success {

        @Test
        @DisplayName("should change status from TODO to IN_PROGRESS")
        void shouldChangeStatusFromTodoToInProgress() {
            UUID taskId = UUID.randomUUID();
            Task task = Task.create("Task title", "Task description", Priority.MEDIUM, LocalDateTime.of(2026, Month.AUGUST, 1, 12, 0).plusDays(1), ownerId);
            ChangeTaskStatusCommand command = new ChangeTaskStatusCommand(taskId, Status.IN_PROGRESS, ownerId);

            when(repositoryPort.findById(taskId)).thenReturn(Optional.of(task));
            when(repositoryPort.update(task)).thenReturn(task);

            Task result = changeTaskStatusService.execute(command);

            assertThat(result.getStatus()).isEqualTo(Status.IN_PROGRESS);
            verify(repositoryPort).findById(taskId);
            verify(repositoryPort).update(task);
        }

        @Test
        @DisplayName("should change status from IN_PROGRESS to DONE")
        void shouldChangeStatusFromInProgressToDone() {
            UUID taskId = UUID.randomUUID();
            Task task = Task.create("Task title", "Task description", Priority.HIGH, LocalDateTime.of(2026, Month.AUGUST, 1, 12, 0).plusDays(2), ownerId);
            task.changeStatus(Status.IN_PROGRESS);
            ChangeTaskStatusCommand command = new ChangeTaskStatusCommand(taskId, Status.DONE, ownerId);

            when(repositoryPort.findById(taskId)).thenReturn(Optional.of(task));
            when(repositoryPort.update(task)).thenReturn(task);

            Task result = changeTaskStatusService.execute(command);

            assertThat(result.getStatus()).isEqualTo(Status.DONE);
            verify(repositoryPort).findById(taskId);
            verify(repositoryPort).update(task);
        }

        @Test
        @DisplayName("should return the task returned by the repository, not the locally mutated one")
        void shouldReturnTaskReturnedByRepository() {
            UUID taskId = UUID.randomUUID();
            Task task = Task.create("Task title", "Task description", Priority.LOW, LocalDateTime.of(2026, Month.AUGUST, 1, 12, 0).plusDays(3), ownerId);
            ChangeTaskStatusCommand command = new ChangeTaskStatusCommand(taskId, Status.IN_PROGRESS, ownerId);

            Task updatedTaskFromRepository = Task.create("Task title", "Task description", Priority.LOW, LocalDateTime.of(2026, Month.AUGUST, 1, 12, 0).plusDays(3), ownerId);
            updatedTaskFromRepository.changeStatus(Status.IN_PROGRESS);

            when(repositoryPort.findById(taskId)).thenReturn(Optional.of(task));
            when(repositoryPort.update(task)).thenReturn(updatedTaskFromRepository);

            Task result = changeTaskStatusService.execute(command);

            assertThat(result).isSameAs(updatedTaskFromRepository);
            verify(repositoryPort).findById(taskId);
            verify(repositoryPort).update(task);
        }
    }

    @Nested
    @DisplayName("Status change with error")
    class WithError {

        @Test
        @DisplayName("should throw TaskNotFoundException when task does not exist")
        void shouldThrowTaskNotFoundExceptionWhenTaskDoesNotExist() {
            UUID taskId = UUID.randomUUID();
            ChangeTaskStatusCommand command = new ChangeTaskStatusCommand(taskId, Status.IN_PROGRESS, ownerId);

            when(repositoryPort.findById(taskId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> changeTaskStatusService.execute(command))
                    .isInstanceOf(TaskNotFoundException.class);

            verify(repositoryPort).findById(taskId);
            verify(repositoryPort, never()).update(any());
        }

        @Test
        @DisplayName("should throw InvalidStatusTransitionException when changing from TODO to DONE")
        void shouldThrowInvalidStatusTransitionExceptionWhenChangingFromTodoToDone() {
            UUID taskId = UUID.randomUUID();
            Task task = Task.create("Task title", "Task description", Priority.MEDIUM, LocalDateTime.of(2026, Month.AUGUST, 1, 12, 0).plusDays(1), ownerId);
            ChangeTaskStatusCommand command = new ChangeTaskStatusCommand(taskId, Status.DONE, ownerId);

            when(repositoryPort.findById(taskId)).thenReturn(Optional.of(task));

            assertThatThrownBy(() -> changeTaskStatusService.execute(command))
                    .isInstanceOf(InvalidStatusTransitionException.class);

            verify(repositoryPort).findById(taskId);
            verify(repositoryPort, never()).update(any());
        }

        @Test
        @DisplayName("should throw InvalidStatusTransitionException when changing from DONE to any status")
        void shouldThrowInvalidStatusTransitionExceptionWhenChangingFromDone() {
            UUID taskId = UUID.randomUUID();
            Task task = Task.create("Task title", "Task description", Priority.HIGH, LocalDateTime.of(2026, Month.AUGUST, 1, 12, 0).plusDays(1), ownerId);
            task.changeStatus(Status.IN_PROGRESS);
            task.changeStatus(Status.DONE);
            ChangeTaskStatusCommand command = new ChangeTaskStatusCommand(taskId, Status.TODO, ownerId);

            when(repositoryPort.findById(taskId)).thenReturn(Optional.of(task));

            assertThatThrownBy(() -> changeTaskStatusService.execute(command))
                    .isInstanceOf(InvalidStatusTransitionException.class);

            verify(repositoryPort).findById(taskId);
            verify(repositoryPort, never()).update(any());
        }
    }
}