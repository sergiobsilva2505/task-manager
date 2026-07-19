package br.com.forjacode.taskmanager.application.service;

import br.com.forjacode.taskmanager.application.ports.input.command.CreateTaskCommand;
import br.com.forjacode.taskmanager.application.ports.output.TaskRepositoryPort;
import br.com.forjacode.taskmanager.domain.exception.MissingRequiredFieldException;
import br.com.forjacode.taskmanager.domain.model.Task;
import br.com.forjacode.taskmanager.domain.model.enums.Priority;
import br.com.forjacode.taskmanager.domain.model.enums.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

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
			LocalDateTime dueDate = LocalDateTime.now().plusDays(1);
			CreateTaskCommand command = new CreateTaskCommand("Pay bills", "Monthly bills", Priority.HIGH, dueDate);
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
			assertThat(createdTask.getCreatedAt()).isNotNull();
			assertThat(createdTask.getUpdatedAt()).isNotNull();
		}
	}

	@Nested
	@DisplayName("Creation with error")
	class WithError {

		private LocalDateTime futureDueDate;

		@BeforeEach
		void setUp() {
			futureDueDate = LocalDateTime.now().plusDays(1);
		}

		@Test
		@DisplayName("should throw exception when title is null")
		void shouldThrowExceptionWhenTitleIsNull() {
			CreateTaskCommand command = new CreateTaskCommand(null, "description", Priority.MEDIUM, futureDueDate);

			assertThatThrownBy(() -> createTaskService.execute(command))
					.isInstanceOf(MissingRequiredFieldException.class);

			verify(repositoryPort, never()).save(any(Task.class));
		}

		@Test
		@DisplayName("should throw exception when title is blank")
		void shouldThrowExceptionWhenTitleIsBlank() {
			CreateTaskCommand command = new CreateTaskCommand("   ", "description", Priority.MEDIUM, futureDueDate);

			assertThatThrownBy(() -> createTaskService.execute(command))
					.isInstanceOf(MissingRequiredFieldException.class);

			verify(repositoryPort, never()).save(any(Task.class));
		}

		@Test
		@DisplayName("should throw exception when priority is null")
		void shouldThrowExceptionWhenPriorityIsNull() {
			CreateTaskCommand command = new CreateTaskCommand("Task", "description", null, futureDueDate);

			assertThatThrownBy(() -> createTaskService.execute(command))
					.isInstanceOf(MissingRequiredFieldException.class);

			verify(repositoryPort, never()).save(any(Task.class));
		}

		@Test
		@DisplayName("should throw exception when due date is null")
		void shouldThrowExceptionWhenDueDateIsNull() {
			CreateTaskCommand command = new CreateTaskCommand("Task", "description", Priority.MEDIUM, null);

			assertThatThrownBy(() -> createTaskService.execute(command))
					.isInstanceOf(MissingRequiredFieldException.class);

			verify(repositoryPort, never()).save(any(Task.class));
		}
	}

}