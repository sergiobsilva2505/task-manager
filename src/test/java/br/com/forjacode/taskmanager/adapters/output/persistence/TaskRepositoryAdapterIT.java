package br.com.forjacode.taskmanager.adapters.output.persistence;

import br.com.forjacode.taskmanager.AbstractIntegrationTest;
import br.com.forjacode.taskmanager.domain.model.Task;
import br.com.forjacode.taskmanager.domain.model.enums.Priority;
import br.com.forjacode.taskmanager.domain.model.enums.Status;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Import({TaskRepositoryAdapter.class, TaskMapperImpl.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaskRepositoryAdapterIT extends AbstractIntegrationTest {

    @Autowired
    private TaskRepositoryAdapter taskRepositoryAdapter;

    @Autowired
    private TaskJpaRepository taskJpaRepository;

    @BeforeEach
    void setUp() {
        taskJpaRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        taskJpaRepository.deleteAll();
    }

    @Nested
    @DisplayName("Success")
    class Success {

        @Test
        @DisplayName("should save and find task by id")
        void shouldSaveAndFindTaskById() {
            Task task = Task.create("Task 1", "description", Priority.HIGH, LocalDateTime.now().plusDays(1));

            taskRepositoryAdapter.save(task);
            Optional<Task> foundTask = taskRepositoryAdapter.findById(task.getId());

            assertThat(foundTask).isPresent();
            assertThat(foundTask.get().getId()).isEqualTo(task.getId());
            assertThat(foundTask.get().getTitle()).isEqualTo("Task 1");
            assertThat(foundTask.get().getDescription()).isEqualTo("description");
            assertThat(foundTask.get().getPriority()).isEqualTo(Priority.HIGH);
            assertThat(foundTask.get().getStatus()).isEqualTo(Status.TODO);
        }

        @Test
        @DisplayName("should return all persisted tasks")
        void shouldReturnAllPersistedTasks() {
            Task firstTask = Task.create("Task 1", "description 1", Priority.HIGH, LocalDateTime.now().plusDays(1));
            Task secondTask = Task.create("Task 2", "description 2", Priority.MEDIUM, LocalDateTime.now().plusDays(2));

            taskRepositoryAdapter.save(firstTask);
            taskRepositoryAdapter.save(secondTask);

            List<Task> tasks = taskRepositoryAdapter.findAll();

            assertThat(tasks).hasSize(2);
            assertThat(tasks).extracting(Task::getTitle).containsExactlyInAnyOrder("Task 1", "Task 2");
        }

        @Test
        @DisplayName("should update an existing task")
        void shouldUpdateAnExistingTask() {
            Task task = Task.create("Task 1", "description", Priority.LOW, LocalDateTime.now().plusDays(1));
            taskRepositoryAdapter.save(task);
            task.changeStatus(Status.IN_PROGRESS);

            Task updatedTask = taskRepositoryAdapter.update(task);

            assertThat(updatedTask.getId()).isEqualTo(task.getId());
            assertThat(updatedTask.getStatus()).isEqualTo(Status.IN_PROGRESS);
            assertThat(updatedTask.getUpdatedAt()).isAfterOrEqualTo(updatedTask.getCreatedAt());
            assertThat(taskRepositoryAdapter.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("should delete task")
        void shouldDeleteTask() {
            Task task = Task.create("Task 1", "description", Priority.HIGH, LocalDateTime.now().plusDays(1));
            taskRepositoryAdapter.save(task);

            taskRepositoryAdapter.delete(task);

            Optional<Task> foundTask = taskRepositoryAdapter.findById(task.getId());
            assertThat(foundTask).isEmpty();
        }
    }

    @Nested
    @DisplayName("WithError")
    class WithError {

        @Test
        @DisplayName("should return empty when id does not exist")
        void shouldReturnEmptyWhenIdDoesNotExist() {
            Optional<Task> foundTask = taskRepositoryAdapter.findById(UUID.randomUUID());

            assertThat(foundTask).isEmpty();
        }

        @Test
        @DisplayName("should return empty list when there are no persisted tasks")
        void shouldReturnEmptyListWhenThereAreNoPersistedTasks() {
            List<Task> tasks = taskRepositoryAdapter.findAll();

            assertThat(tasks).isEmpty();
        }
    }

}