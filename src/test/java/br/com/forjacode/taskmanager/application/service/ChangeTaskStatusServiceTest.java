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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangeTaskStatusServiceTest {

    @Mock
    private TaskRepositoryPort repositoryPort;

    @InjectMocks
    private ChangeTaskStatusService changeTaskStatusService;

    @Nested
    @DisplayName("Successful status change")
    class Success {

        @Test
        @DisplayName("should change status from TODO to IN_PROGRESS")
        void shouldChangeStatusFromTodoToInProgress() {
            UUID taskId = UUID.randomUUID();
            Task task = Task.create("Task title", "Task description", Priority.MEDIUM, LocalDateTime.now().plusDays(1));
            ChangeTaskStatusCommand command = new ChangeTaskStatusCommand(taskId, Status.IN_PROGRESS);

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
            Task task = Task.create("Task title", "Task description", Priority.HIGH, LocalDateTime.now().plusDays(2));
            task.changeStatus(Status.IN_PROGRESS);
            ChangeTaskStatusCommand command = new ChangeTaskStatusCommand(taskId, Status.DONE);

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
            Task task = Task.create("Task title", "Task description", Priority.LOW, LocalDateTime.now().plusDays(3));
            ChangeTaskStatusCommand command = new ChangeTaskStatusCommand(taskId, Status.IN_PROGRESS);

            Task updatedTaskFromRepository = Task.create("Task title", "Task description", Priority.LOW, LocalDateTime.now().plusDays(3));
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
            ChangeTaskStatusCommand command = new ChangeTaskStatusCommand(taskId, Status.IN_PROGRESS);

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
            Task task = Task.create("Task title", "Task description", Priority.MEDIUM, LocalDateTime.now().plusDays(1));
            ChangeTaskStatusCommand command = new ChangeTaskStatusCommand(taskId, Status.DONE);

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
            Task task = Task.create("Task title", "Task description", Priority.HIGH, LocalDateTime.now().plusDays(1));
            task.changeStatus(Status.IN_PROGRESS);
            task.changeStatus(Status.DONE);
            ChangeTaskStatusCommand command = new ChangeTaskStatusCommand(taskId, Status.TODO);

            when(repositoryPort.findById(taskId)).thenReturn(Optional.of(task));

            assertThatThrownBy(() -> changeTaskStatusService.execute(command))
                    .isInstanceOf(InvalidStatusTransitionException.class);

            verify(repositoryPort).findById(taskId);
            verify(repositoryPort, never()).update(any());
        }
    }
}