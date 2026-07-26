package br.com.forjacode.taskmanager.adapters.input.rest;

import br.com.forjacode.taskmanager.AbstractIntegrationTest;
import br.com.forjacode.taskmanager.adapters.input.rest.dto.TaskResponse;
import br.com.forjacode.taskmanager.adapters.output.persistence.TaskJpaRepository;
import br.com.forjacode.taskmanager.adapters.output.persistence.UserJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static br.com.forjacode.taskmanager.testsuport.UserJpaEntityBuilder.aSecondUser;
import static br.com.forjacode.taskmanager.testsuport.UserJpaEntityBuilder.anUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskJpaRepository taskJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID userId;

    @BeforeEach
    void setUp() {
        taskJpaRepository.deleteAll();
        userJpaRepository.deleteAll();

        var user = anUser();
        userId = user.getId();
        userJpaRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        taskJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
    }

    @Nested
    @DisplayName("Create task")
    class CreateTask {

        @Nested
        @DisplayName("Success")
        class Success {

            @Test
            @DisplayName("should create task with valid request")
            void shouldCreateTaskWithValidRequest() throws Exception {
                mockMvc.perform(post("/api/tasks")
                                .header("X-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task","description":"desc","priority":"HIGH","dueDate":"2026-08-01T10:00:00"}
                                        """))
                        .andExpect(status().isCreated())
                        .andExpect(header().exists("Location"))
                        .andExpect(jsonPath("$.id").isNotEmpty())
                        .andExpect(jsonPath("$.title").value("Task"))
                        .andExpect(jsonPath("$.description").value("desc"))
                        .andExpect(jsonPath("$.priority").value("HIGH"))
                        .andExpect(jsonPath("$.status").value("TODO"));
                assertThat(taskJpaRepository.count()).isEqualTo(1);
            }
        }

        @Nested
        @DisplayName("WithError")
        class WithError {

            @Test
            @DisplayName("should return bad request when title is blank")
            void shouldReturnBadRequestWhenTitleIsBlank() throws Exception {
                mockMvc.perform(post("/api/tasks")
                                .header("X-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"","description":"desc","priority":"HIGH","dueDate":"2026-08-01T10:00:00"}
                                        """))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Validation Failed"))
                        .andExpect(jsonPath("$.errors.title").exists());
                assertThat(taskJpaRepository.count()).isZero();
            }

            @Test
            @DisplayName("should return bad request when due date is in the past")
            void shouldReturnBadRequestWhenDueDateIsInThePast() throws Exception {
                mockMvc.perform(post("/api/tasks")
                                .header("X-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task","description":"desc","priority":"HIGH","dueDate":"2026-07-24T10:00:00"}
                                        """))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Validation Failed"))
                        .andExpect(jsonPath("$.errors.dueDate").exists());
                assertThat(taskJpaRepository.count()).isZero();
            }

            @Test
            @DisplayName("should return bad request when priority is null")
            void shouldReturnBadRequestWhenPriorityIsNull() throws Exception {
                mockMvc.perform(post("/api/tasks")
                                .header("X-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task","description":"desc","priority":null,"dueDate":"2026-08-01T10:00:00"}
                                        """))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Validation Failed"))
                        .andExpect(jsonPath("$.errors.priority").exists());
                assertThat(taskJpaRepository.count()).isZero();
            }
        }
    }

    @Nested
    @DisplayName("Get task by id")
    class GetTaskById {

        @Nested
        @DisplayName("Success")
        class Success {

            @Test
            @DisplayName("should return task when id exists")
            void shouldReturnTaskWhenIdExists() throws Exception {
                String createResponse = mockMvc.perform(post("/api/tasks")
                                .header("X-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"My Task","description":"Task description","priority":"HIGH","dueDate":"2026-08-01T10:00:00"}
                                        """))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                TaskResponse createdTask = objectMapper.readValue(createResponse, TaskResponse.class);

                mockMvc.perform(get("/api/tasks/{taskId}", createdTask.id())
                                .header("X-User-Id", userId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(createdTask.id().toString()))
                        .andExpect(jsonPath("$.title").value("My Task"))
                        .andExpect(jsonPath("$.description").value("Task description"))
                        .andExpect(jsonPath("$.priority").value("HIGH"))
                        .andExpect(jsonPath("$.status").value("TODO"));
            }
        }

        @Nested
        @DisplayName("WithError")
        class WithError {

            @Test
            @DisplayName("should return not found when task does not exist")
            void shouldReturnNotFoundWhenTaskDoesNotExist() throws Exception {
                String nonExistentId = "00000000-0000-0000-0000-000000000000";

                mockMvc.perform(get("/api/tasks/{taskId}", nonExistentId)
                                .header("X-User-Id", userId))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.title").value("Task Not Found"))
                        .andExpect(jsonPath("$.detail").value(
                                containsString("Task with ID %s not found".formatted(nonExistentId))));
            }

            @Test
            @DisplayName("should return not found when task belongs to another user")
            void shouldReturnNotFoundWhenTaskBelongsToAnotherUser() throws Exception {
                // Cria tarefa com o primeiro usuário
                String createResponse = mockMvc.perform(post("/api/tasks")
                                .header("X-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"My Task","description":"Task description","priority":"HIGH","dueDate":"2026-08-01T10:00:00"}
                                        """))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                TaskResponse createdTask = objectMapper.readValue(createResponse, TaskResponse.class);

                // Cria um segundo usuário com email único
                var secondUser = aSecondUser();

                UUID secondUserId = secondUser.getId();
                userJpaRepository.save(secondUser);

                // Tenta buscar a tarefa usando o segundo usuário
                mockMvc.perform(get("/api/tasks/{taskId}", createdTask.id())
                                .header("X-User-Id", secondUserId))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.title").value("Task Not Found"));
            }
        }
    }

    @Nested
    @DisplayName("List tasks")
    class ListTasks {

        @Nested
        @DisplayName("Success")
        class Success {

            @Test
            @DisplayName("should return paged tasks when request contains valid pagination and sorting")
            void shouldReturnPagedTasksWhenRequestContainsValidPaginationAndSorting() throws Exception {
                mockMvc.perform(post("/api/tasks")
                                .header("X-User-Id", userId)

                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task B","description":"desc","priority":"HIGH","dueDate":"2026-08-01T10:00:00"}
                                        """))
                        .andExpect(status().isCreated());

                mockMvc.perform(post("/api/tasks")
                                .header("X-User-Id", userId)

                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task A","description":"desc","priority":"MEDIUM","dueDate":"2026-08-02T10:00:00"}
                                        """))
                        .andExpect(status().isCreated());

                mockMvc.perform(get("/api/tasks")
                                .header("X-User-Id", userId)
                                .param("page", "0")
                                .param("size", "1")
                                .param("sortField", "TITLE")
                                .param("sortDirection", "ASC"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content.length()").value(1))
                        .andExpect(jsonPath("$.content[0].title").value("Task A"))
                        .andExpect(jsonPath("$.page").value(0))
                        .andExpect(jsonPath("$.size").value(1))
                        .andExpect(jsonPath("$.totalElements").value(2))
                        .andExpect(jsonPath("$.totalPages").value(2));
            }

            @Test
            @DisplayName("should use default pagination and sorting when query params are omitted")
            void shouldUseDefaultPaginationAndSortingWhenQueryParamsAreOmitted() throws Exception {
                mockMvc.perform(post("/api/tasks")
                                .header("X-User-Id", userId)

                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task 1","description":"desc","priority":"HIGH","dueDate":"2026-08-01T10:00:00"}
                                        """))
                        .andExpect(status().isCreated());

                mockMvc.perform(post("/api/tasks")
                                .header("X-User-Id", userId)

                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task 2","description":"desc","priority":"LOW","dueDate":"2026-08-03T10:00:00"}
                                        """))
                        .andExpect(status().isCreated());

                mockMvc.perform(get("/api/tasks")
                                .header("X-User-Id", userId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.page").value(0))
                        .andExpect(jsonPath("$.size").value(20))
                        .andExpect(jsonPath("$.totalElements").value(2))
                        .andExpect(jsonPath("$.content.length()").value(2));
            }

            @Test
            @DisplayName("should only return tasks owned by the user (isolation)")
            void shouldOnlyReturnTasksOwnedByTheUser() throws Exception {
                // Usuário A cria duas tarefas
                mockMvc.perform(post("/api/tasks")
                                .header("X-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"User A Task 1","description":"desc","priority":"HIGH","dueDate":"2026-08-01T10:00:00"}
                                        """))
                        .andExpect(status().isCreated());

                mockMvc.perform(post("/api/tasks")
                                .header("X-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"User A Task 2","description":"desc","priority":"MEDIUM","dueDate":"2026-08-02T10:00:00"}
                                        """))
                        .andExpect(status().isCreated());

                // Cria um segundo usuário
                var secondUser = aSecondUser();
                UUID secondUserId = secondUser.getId();
                userJpaRepository.save(secondUser);

                // Usuário B cria uma tarefa
                mockMvc.perform(post("/api/tasks")
                                .header("X-User-Id", secondUserId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"User B Task 1","description":"desc","priority":"LOW","dueDate":"2026-08-03T10:00:00"}
                                        """))
                        .andExpect(status().isCreated());

                // CRITICAL: Usuário B lista suas tarefas e vê apenas a própria
                mockMvc.perform(get("/api/tasks")
                                .header("X-User-Id", secondUserId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(1))
                        .andExpect(jsonPath("$.content.length()").value(1))
                        .andExpect(jsonPath("$.content[0].title").value("User B Task 1"));

                // Confirma que usuário A ainda vê suas 2 tarefas
                mockMvc.perform(get("/api/tasks")
                                .header("X-User-Id", userId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(2))
                        .andExpect(jsonPath("$.content.length()").value(2))
                        .andExpect(jsonPath("$.content[*].title").value(
                                org.hamcrest.Matchers.containsInAnyOrder("User A Task 1", "User A Task 2")));

                // Verificação adicional: total no banco deve ser 3 tarefas
                assertThat(taskJpaRepository.count()).isEqualTo(3);
            }
        }

        @Nested
        @DisplayName("WithError")
        class WithError {

            @Test
            @DisplayName("should return bad request when sort field is invalid")
            void shouldReturnBadRequestWhenSortFieldIsInvalid() throws Exception {
                mockMvc.perform(get("/api/tasks")
                                .header("X-User-Id", userId)
                                .param("sortField", "INVALID_SORT")
                                .param("sortDirection", "ASC"))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Invalid Parameter Type"))
                        .andExpect(jsonPath("$.detail").value(containsString("sortField")));
            }

            @Test
            @DisplayName("should return empty content when requested page exceeds available pages")
            void shouldReturnEmptyContentWhenRequestedPageExceedsAvailablePages() throws Exception {
                mockMvc.perform(post("/api/tasks")
                                .header("X-User-Id", userId)

                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task only","description":"desc","priority":"HIGH","dueDate":"2026-08-01T10:00:00"}
                                        """))
                        .andExpect(status().isCreated());

                mockMvc.perform(get("/api/tasks")
                                .header("X-User-Id", userId)
                                .param("page", "3")
                                .param("size", "10"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content.length()").value(0))
                        .andExpect(jsonPath("$.totalElements").value(1))
                        .andExpect(jsonPath("$.totalPages").value(1));
            }
        }
    }

    @Nested
    @DisplayName("Change task status")
    class ChangeTaskStatus {

        @Nested
        @DisplayName("Success")
        class Success {

            @Test
            @DisplayName("should change status when transition is valid")
            void shouldChangeStatusWhenTransitionIsValid() throws Exception {
                String createResponse = mockMvc.perform(post("/api/tasks")
                                .header("X-User-Id", userId)

                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task","description":"desc","priority":"HIGH","dueDate":"2026-09-01T10:00:00"}
                                        """))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                TaskResponse createdTask = objectMapper.readValue(createResponse, TaskResponse.class);

                mockMvc.perform(patch("/api/tasks/{taskId}/status", createdTask.id())
                                .header("X-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"status":"IN_PROGRESS"}
                                        """))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(createdTask.id().toString()))
                        .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                        .andExpect(jsonPath("$.title").value("Task"));
            }
        }

        @Nested
        @DisplayName("WithError")
        class WithError {

            @Test
            @DisplayName("should return not found when task does not exist")
            void shouldReturnNotFoundWhenTaskDoesNotExist() throws Exception {
                String nonExistentId = "00000000-0000-0000-0000-000000000000";

                mockMvc.perform(patch("/api/tasks/{taskId}/status", nonExistentId)
                                .header("X-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"status":"IN_PROGRESS"}
                                        """))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.title").value("Task Not Found"))
                        .andExpect(jsonPath("$.detail").value(
                                containsString("Task with ID %s not found".formatted(nonExistentId))));
            }

            @Test
            @DisplayName("should return bad request when transition is invalid")
            void shouldReturnBadRequestWhenTransitionIsInvalid() throws Exception {
                String createResponse = mockMvc.perform(post("/api/tasks")
                                .header("X-User-Id", userId)

                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task","description":"desc","priority":"HIGH","dueDate":"2026-09-01T10:00:00"}
                                        """))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                TaskResponse createdTask = objectMapper.readValue(createResponse, TaskResponse.class);

                mockMvc.perform(patch("/api/tasks/{taskId}/status", createdTask.id())
                                .header("X-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"status":"DONE"}
                                        """))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Invalid Status Transition"))
                        .andExpect(jsonPath("$.detail").value("Cannot change status from TODO to DONE"));
            }

            @Test
            @DisplayName("should return bad request when status is missing")
            void shouldReturnBadRequestWhenStatusIsMissing() throws Exception {
                String createResponse = mockMvc.perform(post("/api/tasks")
                                .header("X-User-Id", userId)

                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task","description":"desc","priority":"HIGH","dueDate":"2026-09-01T10:00:00"}
                                        """))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                TaskResponse createdTask = objectMapper.readValue(createResponse, TaskResponse.class);

                mockMvc.perform(patch("/api/tasks/{taskId}/status", createdTask.id())
                                .header("X-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"status":null}
                                        """))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Validation Failed"))
                        .andExpect(jsonPath("$.errors.status").exists());
            }

            @Test
            @DisplayName("should return bad request when status value is invalid")
            void shouldReturnBadRequestWhenStatusValueIsInvalid() throws Exception {
                String createResponse = mockMvc.perform(post("/api/tasks")
                                .header("X-User-Id", userId)

                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task","description":"desc","priority":"HIGH","dueDate":"2026-09-01T10:00:00"}
                                        """))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                TaskResponse createdTask = objectMapper.readValue(createResponse, TaskResponse.class);

                mockMvc.perform(patch("/api/tasks/{taskId}/status", createdTask.id())
                                .header("X-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"status":"NAOEXISTE"}
                                        """))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Malformed JSON"));
            }

            @Test
            @DisplayName("should return not found when task belongs to another user")
            void shouldReturnNotFoundWhenTaskBelongsToAnotherUser() throws Exception {
                // Cria tarefa com o primeiro usuário
                String createResponse = mockMvc.perform(post("/api/tasks")
                                .header("X-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task","description":"desc","priority":"HIGH","dueDate":"2026-09-01T10:00:00"}
                                        """))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                TaskResponse createdTask = objectMapper.readValue(createResponse, TaskResponse.class);

                // Cria um segundo usuário
                var secondUser = aSecondUser();
                UUID secondUserId = secondUser.getId();
                userJpaRepository.save(secondUser);

                // Tenta alterar o status da tarefa usando o segundo usuário
                mockMvc.perform(patch("/api/tasks/{taskId}/status", createdTask.id())
                                .header("X-User-Id", secondUserId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"status":"IN_PROGRESS"}
                                        """))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.title").value("Task Not Found"));
            }
        }
    }

    @Nested
    @DisplayName("Delete task")
    class DeleteTask {

        @Nested
        @DisplayName("Success")
        class Success {

            @Test
            @DisplayName("should delete task when id exists")
            void shouldDeleteTaskWhenIdExists() throws Exception {
                String createResponse = mockMvc.perform(post("/api/tasks")
                                .header("X-User-Id", userId)

                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task to Delete","description":"desc","priority":"HIGH","dueDate":"2026-08-01T10:00:00"}
                                        """))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                TaskResponse createdTask = objectMapper.readValue(createResponse, TaskResponse.class);

                mockMvc.perform(delete("/api/tasks/{taskId}", createdTask.id())
                                .header("X-User-Id", userId))
                        .andExpect(status().isNoContent());

                assertThat(taskJpaRepository.count()).isZero();
            }

            @Test
            @DisplayName("should return no content when task does not exist (idempotent)")
            void shouldReturnNoContentWhenTaskDoesNotExist() throws Exception {
                String nonExistentId = "00000000-0000-0000-0000-000000000000";

                mockMvc.perform(delete("/api/tasks/{taskId}", nonExistentId)
                                .header("X-User-Id", userId))
                        .andExpect(status().isNoContent());
            }

            @Test
            @DisplayName("should not delete task when another user tries to delete it (isolation)")
            void shouldNotDeleteTaskWhenAnotherUserTriesToDeleteIt() throws Exception {
                // Cria tarefa com o primeiro usuário
                String createResponse = mockMvc.perform(post("/api/tasks")
                                .header("X-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task to Delete","description":"desc","priority":"HIGH","dueDate":"2026-08-01T10:00:00"}
                                        """))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                TaskResponse createdTask = objectMapper.readValue(createResponse, TaskResponse.class);

                // Cria um segundo usuário
                var secondUser = aSecondUser();
                UUID secondUserId = secondUser.getId();
                userJpaRepository.save(secondUser);

                // Segundo usuário tenta deletar a tarefa do primeiro usuário
                // A resposta é 204 (idempotente - como se a tarefa não existisse para ele)
                mockMvc.perform(delete("/api/tasks/{taskId}", createdTask.id())
                                .header("X-User-Id", secondUserId))
                        .andExpect(status().isNoContent());

                // CRITICAL: Verifica que a tarefa NÃO foi deletada
                // Ainda deve existir no banco e ser acessível pelo dono
                assertThat(taskJpaRepository.count()).isEqualTo(1);

                // Confirma que o primeiro usuário ainda pode acessar sua tarefa
                mockMvc.perform(get("/api/tasks/{taskId}", createdTask.id())
                                .header("X-User-Id", userId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(createdTask.id().toString()))
                        .andExpect(jsonPath("$.title").value("Task to Delete"));
            }
        }

        @Nested
        @DisplayName("WithError")
        class WithError {

            @Test
            @DisplayName("should return bad request when task id format is invalid")
            void shouldReturnBadRequestWhenTaskIdFormatIsInvalid() throws Exception {
                String invalidId = "invalid-uuid-format";

                mockMvc.perform(delete("/api/tasks/{taskId}", invalidId)
                                .header("X-User-Id", userId))
                        .andExpect(status().isBadRequest());
            }
        }
    }
}

