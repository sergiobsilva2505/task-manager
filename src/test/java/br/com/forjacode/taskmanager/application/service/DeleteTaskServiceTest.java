package br.com.forjacode.taskmanager.application.service;

import br.com.forjacode.taskmanager.application.ports.output.TaskRepositoryPort;
import br.com.forjacode.taskmanager.domain.model.Task;
import br.com.forjacode.taskmanager.domain.model.enums.Priority;
import org.junit.jupiter.api.BeforeEach;
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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteTaskServiceTest {

    @Mock
    private TaskRepositoryPort taskRepository;

    @InjectMocks
    private DeleteTaskService deleteTaskService;

    private UUID ownerId;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("Success")
    class Success {

        @Test
        @DisplayName("should delete task when it exists and belongs to the user")
        void shouldDeleteTaskWhenItExistsAndBelongsToTheUser() {
            Task task = Task.create("Task title", "Task description", Priority.MEDIUM,
                    LocalDateTime.now().plusDays(1), ownerId);

            when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

            deleteTaskService.execute(task.getId(), ownerId);

            verify(taskRepository).deleteByIdAndOwnerId(task.getId(), ownerId);
        }
    }

    @Nested
    @DisplayName("WithError")
    class WithError {

        @Test
        @DisplayName("should do nothing when task does not exist")
        void shouldDoNothingWhenTaskDoesNotExist() {
            UUID taskId = UUID.randomUUID();

            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            deleteTaskService.execute(taskId, ownerId);

            verify(taskRepository, never()).deleteByIdAndOwnerId(taskId, ownerId);
        }

        @Test
        @DisplayName("should do nothing when task belongs to another user")
        void shouldDoNothingWhenTaskBelongsToAnotherUser() {
            UUID anotherOwnerId = UUID.randomUUID();
            Task task = Task.create("Task title", "Task description", Priority.MEDIUM,
                    LocalDateTime.now().plusDays(1), anotherOwnerId);

            when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

            deleteTaskService.execute(task.getId(), ownerId);

            verify(taskRepository, never()).deleteByIdAndOwnerId(task.getId(), ownerId);
        }
    }
}