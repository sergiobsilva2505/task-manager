package br.com.forjacode.taskmanager.adapters.output.persistence;

import br.com.forjacode.taskmanager.IntegrationTestSupport;
import br.com.forjacode.taskmanager.application.ports.shared.PageQuery;
import br.com.forjacode.taskmanager.application.ports.shared.PagedResult;
import br.com.forjacode.taskmanager.application.ports.shared.SortDirection;
import br.com.forjacode.taskmanager.application.ports.shared.TaskSortField;
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

import static br.com.forjacode.taskmanager.testsuport.UserJpaEntityBuilder.aSecondUser;
import static br.com.forjacode.taskmanager.testsuport.UserJpaEntityBuilder.anUser;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Import({TaskRepositoryAdapter.class, TaskMapperImpl.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaskRepositoryAdapterIT extends IntegrationTestSupport {

    @Autowired
    private TaskRepositoryAdapter taskRepositoryAdapter;

    @Autowired
    private TaskJpaRepository taskJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    private UUID ownerId;

    @BeforeEach
    void setUp() {
        taskJpaRepository.deleteAll();
        userJpaRepository.deleteAll();

        UserJpaEntity user = anUser();
        ownerId = user.getId();
        userJpaRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        taskJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
    }

    @Nested
    @DisplayName("save()")
    class SaveMethod {

        @Nested
        @DisplayName("Success")
        class Success {

            @Test
            @DisplayName("should persist task and allow retrieval by id")
            void shouldPersistTaskAndAllowRetrievalById() {
                Task task = Task.create("Task 1", "description", Priority.HIGH, LocalDateTime.now().plusDays(1), ownerId);

                taskRepositoryAdapter.save(task);
                Optional<Task> foundTask = taskRepositoryAdapter.findById(task.getId());

                assertThat(foundTask).isPresent();
                assertThat(foundTask.get().getId()).isEqualTo(task.getId());
                assertThat(foundTask.get().getTitle()).isEqualTo("Task 1");
                assertThat(foundTask.get().getDescription()).isEqualTo("description");
                assertThat(foundTask.get().getPriority()).isEqualTo(Priority.HIGH);
                assertThat(foundTask.get().getStatus()).isEqualTo(Status.TODO);
            }
        }

        @Nested
        @DisplayName("WithError")
        class WithError {

            @Test
            @DisplayName("should keep persisted state unchanged when searching with a different id")
            void shouldKeepPersistedStateUnchangedWhenSearchingWithADifferentId() {
                Task task = Task.create("Task 1", "description", Priority.HIGH, LocalDateTime.now().plusDays(1), ownerId);
                taskRepositoryAdapter.save(task);

                Optional<Task> foundTask = taskRepositoryAdapter.findById(UUID.randomUUID());

                assertThat(foundTask).isEmpty();
                assertThat(taskRepositoryAdapter.findAll()).hasSize(1);
            }
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindByIdMethod {

        @Nested
        @DisplayName("Success")
        class Success {

            @Test
            @DisplayName("should return task when id exists")
            void shouldReturnTaskWhenIdExists() {
                Task task = Task.create("Task 1", "description", Priority.HIGH, LocalDateTime.now().plusDays(1), ownerId);
                taskRepositoryAdapter.save(task);

                Optional<Task> foundTask = taskRepositoryAdapter.findById(task.getId());

                assertThat(foundTask).isPresent();
                assertThat(foundTask.get().getId()).isEqualTo(task.getId());
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
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateMethod {

        @Nested
        @DisplayName("Success")
        class Success {

            @Test
            @DisplayName("should persist changes made to an existing task")
            void shouldPersistChangesMadeToAnExistingTask() {
                Task task = Task.create("Task 1", "description", Priority.HIGH, LocalDateTime.now().plusDays(1), ownerId);
                taskRepositoryAdapter.save(task);

                task.changeStatus(Status.IN_PROGRESS);
                Task updatedTask = taskRepositoryAdapter.update(task);

                assertThat(updatedTask.getStatus()).isEqualTo(Status.IN_PROGRESS);

                Optional<Task> foundTask = taskRepositoryAdapter.findById(task.getId());
                assertThat(foundTask).isPresent();
                assertThat(foundTask.get().getStatus()).isEqualTo(Status.IN_PROGRESS);
            }
        }

        @Nested
        @DisplayName("WithError")
        class WithError {

            @Test
            @DisplayName("should not affect other persisted tasks when updating one task")
            void shouldNotAffectOtherPersistedTasksWhenUpdatingOneTask() {
                Task firstTask = Task.create("Task 1", "description 1", Priority.HIGH, LocalDateTime.now().plusDays(1), ownerId);
                Task secondTask = Task.create("Task 2", "description 2", Priority.MEDIUM,
                        LocalDateTime.now().plusDays(2), ownerId);
                taskRepositoryAdapter.save(firstTask);
                taskRepositoryAdapter.save(secondTask);

                firstTask.changeStatus(Status.IN_PROGRESS);
                taskRepositoryAdapter.update(firstTask);

                Optional<Task> untouchedTask = taskRepositoryAdapter.findById(secondTask.getId());
                assertThat(untouchedTask).isPresent();
                assertThat(untouchedTask.get().getStatus()).isEqualTo(Status.TODO);
            }
        }
    }

    @Nested
    @DisplayName("findAll()")
    class FindAllMethod {

        @Nested
        @DisplayName("Success")
        class Success {

            @Test
            @DisplayName("should return all persisted tasks")
            void shouldReturnAllPersistedTasks() {
                Task firstTask = Task.create("Task 1", "description 1", Priority.HIGH, LocalDateTime.now().plusDays(1), ownerId);
                Task secondTask = Task.create("Task 2", "description 2", Priority.MEDIUM,
                        LocalDateTime.now().plusDays(2), ownerId);

                taskRepositoryAdapter.save(firstTask);
                taskRepositoryAdapter.save(secondTask);

                List<Task> tasks = taskRepositoryAdapter.findAll();

                assertThat(tasks).hasSize(2);
                assertThat(tasks).extracting(Task::getTitle).containsExactlyInAnyOrder("Task 1", "Task 2");
            }
        }

        @Nested
        @DisplayName("WithError")
        class WithError {

            @Test
            @DisplayName("should return empty list when there are no persisted tasks")
            void shouldReturnEmptyListWhenThereAreNoPersistedTasks() {
                List<Task> tasks = taskRepositoryAdapter.findAll();

                assertThat(tasks).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("findAll() paged")
    class FindAllPagedMethod {

        @Nested
        @DisplayName("Success")
        class Success {

            @Test
            @DisplayName("should return paged tasks with metadata when query is valid")
            void shouldReturnPagedTasksWithMetadataWhenQueryIsValid() {
                Task firstTask = Task.create("Task A", "description 1", Priority.HIGH, LocalDateTime.now().plusDays(1), ownerId);
                Task secondTask = Task.create("Task B", "description 2", Priority.MEDIUM,
                        LocalDateTime.now().plusDays(2), ownerId);
                Task thirdTask = Task.create("Task C", "description 3", Priority.LOW, LocalDateTime.now().plusDays(3), ownerId);

                taskRepositoryAdapter.save(firstTask);
                taskRepositoryAdapter.save(secondTask);
                taskRepositoryAdapter.save(thirdTask);

                PageQuery query = new PageQuery(0, 2, TaskSortField.CREATED_AT, SortDirection.DESC);

                PagedResult<Task> result = taskRepositoryAdapter.findAll(query, ownerId);

                assertThat(result.content()).hasSize(2);
                assertThat(result.page()).isZero();
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.totalElements()).isEqualTo(3);
                assertThat(result.totalPages()).isEqualTo(2);
            }

            @Test
            @DisplayName("should return tasks sorted by due date ascending when requested")
            void shouldReturnTasksSortedByDueDateAscendingWhenRequested() {
                Task latestDueDateTask = Task.create("Task 3", "description 3", Priority.HIGH,
                        LocalDateTime.now().plusDays(3), ownerId);
                Task earliestDueDateTask = Task.create("Task 1", "description 1", Priority.MEDIUM,
                        LocalDateTime.now().plusDays(1), ownerId);
                Task middleDueDateTask = Task.create("Task 2", "description 2", Priority.LOW,
                        LocalDateTime.now().plusDays(2), ownerId);

                taskRepositoryAdapter.save(latestDueDateTask);
                taskRepositoryAdapter.save(earliestDueDateTask);
                taskRepositoryAdapter.save(middleDueDateTask);

                PageQuery query = new PageQuery(0, 10, TaskSortField.DUE_DATE, SortDirection.ASC);

                PagedResult<Task> result = taskRepositoryAdapter.findAll(query, ownerId);

                assertThat(result.content()).extracting(Task::getTitle)
                        .containsExactly("Task 1", "Task 2", "Task 3");
            }
        }

        @Nested
        @DisplayName("WithError")
        class WithError {

            @Test
            @DisplayName("should return empty page when there are no persisted tasks")
            void shouldReturnEmptyPageWhenThereAreNoPersistedTasks() {
                PageQuery query = new PageQuery(0, 5, TaskSortField.TITLE, SortDirection.ASC);

                PagedResult<Task> result = taskRepositoryAdapter.findAll(query, ownerId);

                assertThat(result.content()).isEmpty();
                assertThat(result.totalElements()).isZero();
                assertThat(result.totalPages()).isZero();
                assertThat(result.page()).isZero();
                assertThat(result.size()).isEqualTo(5);
            }

            @Test
            @DisplayName("should return empty content when page index is beyond available pages")
            void shouldReturnEmptyContentWhenPageIndexIsBeyondAvailablePages() {
                Task task = Task.create("Only Task", "description", Priority.HIGH, LocalDateTime.now().plusDays(1), ownerId);
                taskRepositoryAdapter.save(task);

                PageQuery query = new PageQuery(2, 1, TaskSortField.CREATED_AT, SortDirection.DESC);

                PagedResult<Task> result = taskRepositoryAdapter.findAll(query, ownerId);

                assertThat(result.content()).isEmpty();
                assertThat(result.totalElements()).isEqualTo(1);
                assertThat(result.totalPages()).isEqualTo(1);
                assertThat(result.page()).isEqualTo(2);
                assertThat(result.size()).isEqualTo(1);
            }
        }
    }

    @Nested
    @DisplayName("deleteByIdAndOwnerId()")
    class DeleteByIdAndOwnerIdMethod {

        @Nested
        @DisplayName("Success")
        class Success {

            @Test
            @DisplayName("should remove task when id and owner id match")
            void shouldRemoveTaskWhenIdAndOwnerIdMatch() {
                Task task = Task.create("Task 1", "description", Priority.HIGH, LocalDateTime.now().plusDays(1), ownerId);
                taskRepositoryAdapter.save(task);

                taskRepositoryAdapter.deleteByIdAndOwnerId(task.getId(), ownerId);

                assertThat(taskRepositoryAdapter.findById(task.getId())).isEmpty();
            }
        }

        @Nested
        @DisplayName("WithError")
        class WithError {

            @Test
            @DisplayName("should keep task persisted when owner id does not match")
            void shouldKeepTaskPersistedWhenOwnerIdDoesNotMatch() {
                Task task = Task.create("Task 1", "description", Priority.HIGH, LocalDateTime.now().plusDays(1), ownerId);
                taskRepositoryAdapter.save(task);

                taskRepositoryAdapter.deleteByIdAndOwnerId(task.getId(), UUID.randomUUID());

                assertThat(taskRepositoryAdapter.findById(task.getId())).isPresent();
            }
        }
    }

    @Nested
    @DisplayName("findAllByOwnerId()")
    class FindAllByOwnerIdMethod {

        @Nested
        @DisplayName("Success")
        class Success {

            @Test
            @DisplayName("should return only tasks belonging to the given owner")
            void shouldReturnOnlyTasksBelongingToTheGivenOwner() {
                UserJpaEntity otherUser = aSecondUser();
                userJpaRepository.save(otherUser);
                UUID otherOwnerId = otherUser.getId();

                Task ownerTask = Task.create("Task 1", "description 1", Priority.HIGH, LocalDateTime.now().plusDays(1), ownerId);
                Task otherOwnerTask = Task.create("Task 2", "description 2", Priority.MEDIUM,
                        LocalDateTime.now().plusDays(2), otherOwnerId);
                taskRepositoryAdapter.save(ownerTask);
                taskRepositoryAdapter.save(otherOwnerTask);

                List<Task> tasks = taskRepositoryAdapter.findAllByOwnerId(ownerId);

                assertThat(tasks).extracting(Task::getId).containsExactly(ownerTask.getId());
            }
        }

        @Nested
        @DisplayName("WithError")
        class WithError {

            @Test
            @DisplayName("should return empty list when owner has no persisted tasks")
            void shouldReturnEmptyListWhenOwnerHasNoPersistedTasks() {
                List<Task> tasks = taskRepositoryAdapter.findAllByOwnerId(ownerId);

                assertThat(tasks).isEmpty();
            }
        }
    }
}