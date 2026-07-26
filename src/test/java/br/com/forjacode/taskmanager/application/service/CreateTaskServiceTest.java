package br.com.forjacode.taskmanager.application.service;

import br.com.forjacode.taskmanager.application.ports.input.command.CreateTaskCommand;
import br.com.forjacode.taskmanager.application.ports.output.TaskRepositoryPort;
import br.com.forjacode.taskmanager.domain.model.Task;
import br.com.forjacode.taskmanager.domain.model.enums.Priority;
import br.com.forjacode.taskmanager.domain.model.enums.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CreateTaskServiceTest {

    @Mock
    private TaskRepositoryPort repositoryPort;

    @InjectMocks
    private CreateTaskService createTaskService;


    @Nested
    @DisplayName("Successful creation")
    class Success {

        @Test
        @DisplayName("should create task and save it when command is valid")
        void shouldCreateTaskAndSaveItWhenCommandIsValid() {
            LocalDateTime dueDate = LocalDateTime.of(2026, Month.AUGUST, 1, 12, 0).plusDays(1);
            UUID ownerId = UUID.randomUUID();
            CreateTaskCommand command = new CreateTaskCommand("Pay bills", "Monthly bills", Priority.HIGH, dueDate,
                    ownerId);
            ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);

            Task createdTask = createTaskService.execute(command);

            verify(repositoryPort).save(taskCaptor.capture());
            Task savedTask = taskCaptor.getValue();

            assertThat(createdTask).isSameAs(savedTask);
            assertThat(createdTask.getId()).isNotNull();
            assertThat(createdTask.getTitle()).isEqualTo("Pay bills");
            assertThat(createdTask.getDescription()).isEqualTo("Monthly bills");
            assertThat(createdTask.getPriority()).isEqualTo(Priority.HIGH);
            assertThat(createdTask.getStatus()).isEqualTo(Status.TODO);
            assertThat(createdTask.getDueDate()).isEqualTo(dueDate);
            assertThat(createdTask.getOwnerId()).isEqualTo(ownerId);
            assertThat(createdTask.getCreatedAt()).isNotNull();
            assertThat(createdTask.getUpdatedAt()).isNotNull();
        }
    }
}