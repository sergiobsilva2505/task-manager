package br.com.forjacode.taskmanager.adapters.input.rest;

import br.com.forjacode.taskmanager.IntegrationTestSupport;
import br.com.forjacode.taskmanager.adapters.input.rest.dto.LoginResponse;
import br.com.forjacode.taskmanager.adapters.input.rest.dto.TaskResponse;
import br.com.forjacode.taskmanager.adapters.output.persistence.AuthIdentityJpaEntity;
import br.com.forjacode.taskmanager.adapters.output.persistence.AuthIdentityJpaRepository;
import br.com.forjacode.taskmanager.adapters.output.persistence.TaskJpaRepository;
import br.com.forjacode.taskmanager.adapters.output.persistence.UserJpaEntity;
import br.com.forjacode.taskmanager.adapters.output.persistence.UserJpaRepository;
import br.com.forjacode.taskmanager.domain.model.enums.AuthProvider;
import br.com.forjacode.taskmanager.domain.model.enums.Priority;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
class TaskControllerIT extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskJpaRepository taskJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private AuthIdentityJpaRepository authIdentityJpaRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String DEFAULT_PASSWORD = "SenhaForte123!";

    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        taskJpaRepository.deleteAll();
        authIdentityJpaRepository.deleteAll();
        userJpaRepository.deleteAll();

        var user = anUser();
        userJpaRepository.save(user);
        userToken = createAuthIdentityAndLogin(user);
    }

    @AfterEach
    void tearDown() {
        taskJpaRepository.deleteAll();
        authIdentityJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
    }

    private String createAuthIdentityAndLogin(UserJpaEntity user) throws Exception {
        authIdentityJpaRepository.save(new AuthIdentityJpaEntity(
                UUID.randomUUID(), user.getId(), AuthProvider.LOCAL,
                passwordEncoder.encode(DEFAULT_PASSWORD), null, Instant.now()));

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(user.getEmail(), DEFAULT_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, LoginResponse.class).token();
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
                LocalDateTime now = LocalDateTime.now();
                String dueDate = now.plusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                mockMvc.perform(post("/api/tasks")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task","description":"desc","priority":"HIGH","dueDate":"%s"}
                                        """.formatted(dueDate)))
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
                LocalDateTime now = LocalDateTime.now();
                String dueDate = now.plusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                mockMvc.perform(post("/api/tasks")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"","description":"desc","priority":"HIGH","dueDate":"%s"}
                                        """.formatted(dueDate)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Validation Failed"))
                        .andExpect(jsonPath("$.errors.title").exists());
                assertThat(taskJpaRepository.count()).isZero();
            }

            @Test
            @DisplayName("should return bad request when due date is in the past")
            void shouldReturnBadRequestWhenDueDateIsInThePast() throws Exception {
                LocalDateTime now = LocalDateTime.now();
                String pastDate = now.minusDays(5).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                mockMvc.perform(post("/api/tasks")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task","description":"desc","priority":"HIGH","dueDate":"%s"}
                                        """.formatted(pastDate)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Validation Failed"))
                        .andExpect(jsonPath("$.errors.dueDate").exists());
                assertThat(taskJpaRepository.count()).isZero();
            }

            @Test
            @DisplayName("should return bad request when priority is null")
            void shouldReturnBadRequestWhenPriorityIsNull() throws Exception {
                LocalDateTime now = LocalDateTime.now();
                String dueDate = now.plusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                mockMvc.perform(post("/api/tasks")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task","description":"desc","priority":null,"dueDate":"%s"}
                                        """.formatted(dueDate)))
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
                LocalDateTime now = LocalDateTime.now();
                String dueDate = now.plusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                String createResponse = mockMvc.perform(post("/api/tasks")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"My Task","description":"Task description","priority":"HIGH","dueDate":"%s"}
                                        """.formatted(dueDate)))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                TaskResponse createdTask = objectMapper.readValue(createResponse, TaskResponse.class);

                mockMvc.perform(get("/api/tasks/{taskId}", createdTask.id())
                                .header("Authorization", "Bearer " + userToken))
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
                                .header("Authorization", "Bearer " + userToken))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.title").value("Task Not Found"))
                        .andExpect(jsonPath("$.detail").value(
                                containsString("Task with ID %s not found".formatted(nonExistentId))));
            }

            @Test
            @DisplayName("should return not found when task belongs to another user")
            void shouldReturnNotFoundWhenTaskBelongsToAnotherUser() throws Exception {
                LocalDateTime now = LocalDateTime.now();
                String dueDate = now.plusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                // Cria tarefa com o primeiro usuário
                String createResponse = mockMvc.perform(post("/api/tasks")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"My Task","description":"Task description","priority":"HIGH","dueDate":"%s"}
                                        """.formatted(dueDate)))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                TaskResponse createdTask = objectMapper.readValue(createResponse, TaskResponse.class);

                // Cria um segundo usuário com email único
                var secondUser = aSecondUser();
                userJpaRepository.save(secondUser);
                String secondUserToken = createAuthIdentityAndLogin(secondUser);

                // Tenta buscar a tarefa usando o segundo usuário
                mockMvc.perform(get("/api/tasks/{taskId}", createdTask.id())
                                .header("Authorization", "Bearer " + secondUserToken))
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
                LocalDateTime now = LocalDateTime.now();
                String dueDate1 = now.plusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                String dueDate2 = now.plusDays(31).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                mockMvc.perform(post("/api/tasks")
                                .header("Authorization", "Bearer " + userToken)

                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task B","description":"desc","priority":"HIGH","dueDate":"%s"}
                                        """.formatted(dueDate1)))
                        .andExpect(status().isCreated());

                mockMvc.perform(post("/api/tasks")
                                .header("Authorization", "Bearer " + userToken)

                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task A","description":"desc","priority":"MEDIUM","dueDate":"%s"}
                                        """.formatted(dueDate2)))
                        .andExpect(status().isCreated());

                mockMvc.perform(get("/api/tasks")
                                .header("Authorization", "Bearer " + userToken)
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
                LocalDateTime now = LocalDateTime.now();
                String dueDate1 = now.plusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                String dueDate2 = now.plusDays(32).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                mockMvc.perform(post("/api/tasks")
                                .header("Authorization", "Bearer " + userToken)

                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task 1","description":"desc","priority":"HIGH","dueDate":"%s"}
                                        """.formatted(dueDate1)))
                        .andExpect(status().isCreated());

                mockMvc.perform(post("/api/tasks")
                                .header("Authorization", "Bearer " + userToken)

                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task 2","description":"desc","priority":"LOW","dueDate":"%s"}
                                        """.formatted(dueDate2)))
                        .andExpect(status().isCreated());

                mockMvc.perform(get("/api/tasks")
                                .header("Authorization", "Bearer " + userToken))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.page").value(0))
                        .andExpect(jsonPath("$.size").value(20))
                        .andExpect(jsonPath("$.totalElements").value(2))
                        .andExpect(jsonPath("$.content.length()").value(2));
            }

            @Test
            @DisplayName("should only return tasks owned by the user (isolation)")
            void shouldOnlyReturnTasksOwnedByTheUser() throws Exception {
                LocalDateTime now = LocalDateTime.now();
                String dueDate1 = now.plusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                String dueDate2 = now.plusDays(31).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                String dueDate3 = now.plusDays(32).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                // Usuário A cria duas tarefas
                mockMvc.perform(post("/api/tasks")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"User A Task 1","description":"desc","priority":"HIGH","dueDate":"%s"}
                                        """.formatted(dueDate1)))
                        .andExpect(status().isCreated());

                mockMvc.perform(post("/api/tasks")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"User A Task 2","description":"desc","priority":"MEDIUM","dueDate":"%s"}
                                        """.formatted(dueDate2)))
                        .andExpect(status().isCreated());

                // Cria um segundo usuário
                var secondUser = aSecondUser();
                userJpaRepository.save(secondUser);
                String secondUserToken = createAuthIdentityAndLogin(secondUser);

                // Usuário B cria uma tarefa
                mockMvc.perform(post("/api/tasks")
                                .header("Authorization", "Bearer " + secondUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"User B Task 1","description":"desc","priority":"LOW","dueDate":"%s"}
                                        """.formatted(dueDate3)))
                        .andExpect(status().isCreated());

                // CRITICAL: Usuário B lista suas tarefas e vê apenas a própria
                mockMvc.perform(get("/api/tasks")
                                .header("Authorization", "Bearer " + secondUserToken))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalElements").value(1))
                        .andExpect(jsonPath("$.content.length()").value(1))
                        .andExpect(jsonPath("$.content[0].title").value("User B Task 1"));

                // Confirma que usuário A ainda vê suas 2 tarefas
                mockMvc.perform(get("/api/tasks")
                                .header("Authorization", "Bearer " + userToken))
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
                                .header("Authorization", "Bearer " + userToken)
                                .param("sortField", "INVALID_SORT")
                                .param("sortDirection", "ASC"))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Invalid Parameter Type"))
                        .andExpect(jsonPath("$.detail").value(containsString("sortField")));
            }

            @Test
            @DisplayName("should return empty content when requested page exceeds available pages")
            void shouldReturnEmptyContentWhenRequestedPageExceedsAvailablePages() throws Exception {
                LocalDateTime now = LocalDateTime.now();
                String dueDate = now.plusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                mockMvc.perform(post("/api/tasks")
                                .header("Authorization", "Bearer " + userToken)

                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task only","description":"desc","priority":"HIGH","dueDate":"%s"}
                                        """.formatted(dueDate)))
                        .andExpect(status().isCreated());

                mockMvc.perform(get("/api/tasks")
                                .header("Authorization", "Bearer " + userToken)
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
                LocalDateTime now = LocalDateTime.now();
                String dueDate = now.plusDays(25).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                String createResponse = mockMvc.perform(post("/api/tasks")
                                .header("Authorization", "Bearer " + userToken)

                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task","description":"desc","priority":"HIGH","dueDate":"%s"}
                                        """.formatted(dueDate)))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                TaskResponse createdTask = objectMapper.readValue(createResponse, TaskResponse.class);

                mockMvc.perform(patch("/api/tasks/{taskId}/status", createdTask.id())
                                .header("Authorization", "Bearer " + userToken)
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
                                .header("Authorization", "Bearer " + userToken)
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
                LocalDateTime now = LocalDateTime.now();
                String dueDate = now.plusDays(25).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                String createResponse = mockMvc.perform(post("/api/tasks")
                                .header("Authorization", "Bearer " + userToken)

                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task","description":"desc","priority":"HIGH","dueDate":"%s"}
                                        """.formatted(dueDate)))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                TaskResponse createdTask = objectMapper.readValue(createResponse, TaskResponse.class);

                mockMvc.perform(patch("/api/tasks/{taskId}/status", createdTask.id())
                                .header("Authorization", "Bearer " + userToken)
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
                LocalDateTime now = LocalDateTime.now();
                String dueDate = now.plusDays(25).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                String createResponse = mockMvc.perform(post("/api/tasks")
                                .header("Authorization", "Bearer " + userToken)

                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task","description":"desc","priority":"HIGH","dueDate":"%s"}
                                        """.formatted(dueDate)))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                TaskResponse createdTask = objectMapper.readValue(createResponse, TaskResponse.class);

                mockMvc.perform(patch("/api/tasks/{taskId}/status", createdTask.id())
                                .header("Authorization", "Bearer " + userToken)
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
                LocalDateTime now = LocalDateTime.now();
                String dueDate = now.plusDays(25).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                String createResponse = mockMvc.perform(post("/api/tasks")
                                .header("Authorization", "Bearer " + userToken)

                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task","description":"desc","priority":"HIGH","dueDate":"%s"}
                                        """.formatted(dueDate)))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                TaskResponse createdTask = objectMapper.readValue(createResponse, TaskResponse.class);

                mockMvc.perform(patch("/api/tasks/{taskId}/status", createdTask.id())
                                .header("Authorization", "Bearer " + userToken)
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
                LocalDateTime now = LocalDateTime.now();
                String dueDate = now.plusDays(25).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                // Cria tarefa com o primeiro usuário
                String createResponse = mockMvc.perform(post("/api/tasks")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task","description":"desc","priority":"HIGH","dueDate":"%s"}
                                        """.formatted(dueDate)))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                TaskResponse createdTask = objectMapper.readValue(createResponse, TaskResponse.class);

                // Cria um segundo usuário
                var secondUser = aSecondUser();
                userJpaRepository.save(secondUser);
                String secondUserToken = createAuthIdentityAndLogin(secondUser);

                // Tenta alterar o status da tarefa usando o segundo usuário
                mockMvc.perform(patch("/api/tasks/{taskId}/status", createdTask.id())
                                .header("Authorization", "Bearer " + secondUserToken)
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
                LocalDateTime now = LocalDateTime.now();
                String dueDate = now.plusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                String createResponse = mockMvc.perform(post("/api/tasks")
                                .header("Authorization", "Bearer " + userToken)

                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task to Delete","description":"desc","priority":"HIGH","dueDate":"%s"}
                                        """.formatted(dueDate)))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                TaskResponse createdTask = objectMapper.readValue(createResponse, TaskResponse.class);

                mockMvc.perform(delete("/api/tasks/{taskId}", createdTask.id())
                                .header("Authorization", "Bearer " + userToken))
                        .andExpect(status().isNoContent());

                assertThat(taskJpaRepository.count()).isZero();
            }

            @Test
            @DisplayName("should return no content when task does not exist (idempotent)")
            void shouldReturnNoContentWhenTaskDoesNotExist() throws Exception {
                String nonExistentId = "00000000-0000-0000-0000-000000000000";

                mockMvc.perform(delete("/api/tasks/{taskId}", nonExistentId)
                                .header("Authorization", "Bearer " + userToken))
                        .andExpect(status().isNoContent());
            }

            @Test
            @DisplayName("should not delete task when another user tries to delete it (isolation)")
            void shouldNotDeleteTaskWhenAnotherUserTriesToDeleteIt() throws Exception {
                LocalDateTime now = LocalDateTime.now();
                String dueDate = now.plusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                // Cria tarefa com o primeiro usuário
                String createResponse = mockMvc.perform(post("/api/tasks")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task to Delete","description":"desc","priority":"HIGH","dueDate":"%s"}
                                        """.formatted(dueDate)))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                TaskResponse createdTask = objectMapper.readValue(createResponse, TaskResponse.class);

                // Cria um segundo usuário
                var secondUser = aSecondUser();
                userJpaRepository.save(secondUser);
                String secondUserToken = createAuthIdentityAndLogin(secondUser);

                // Segundo usuário tenta deletar a tarefa do primeiro usuário
                // A resposta é 204 (idempotente - como se a tarefa não existisse para ele)
                mockMvc.perform(delete("/api/tasks/{taskId}", createdTask.id())
                                .header("Authorization", "Bearer " + secondUserToken))
                        .andExpect(status().isNoContent());

                // CRITICAL: Verifica que a tarefa NÃO foi deletada
                // Ainda deve existir no banco e ser acessível pelo dono
                assertThat(taskJpaRepository.count()).isEqualTo(1);

                // Confirma que o primeiro usuário ainda pode acessar sua tarefa
                mockMvc.perform(get("/api/tasks/{taskId}", createdTask.id())
                                .header("Authorization", "Bearer " + userToken))
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
                                .header("Authorization", "Bearer " + userToken))
                        .andExpect(status().isBadRequest());
            }
        }
    }

    @Nested
    @DisplayName("Dashboard")
    class Dashboard {

        @Nested
        @DisplayName("Success")
        class Success {

            @Test
            @DisplayName("should compute correct metrics for a varied set of tasks and statuses")
            void shouldComputeCorrectMetricsForAVariedSetOfTasksAndStatuses() throws Exception {
                LocalDateTime now = LocalDateTime.now();
                String dueSoonDate = now.plusDays(3).withSecond(0).withNano(0)
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                String farFutureDate1 = now.plusDays(330).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                String farFutureDate2 = now.plusDays(390).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                createTask("Task A", Priority.HIGH, farFutureDate1);

                UUID taskBId = createTask("Task B", Priority.MEDIUM, dueSoonDate);
                changeStatus(taskBId, "IN_PROGRESS");

                UUID taskCId = createTask("Task C", Priority.LOW, farFutureDate1);
                changeStatus(taskCId, "IN_PROGRESS");
                changeStatus(taskCId, "DONE");

                UUID taskDId = createTask("Task D", Priority.HIGH, farFutureDate2);
                changeStatus(taskDId, "CANCELLED");

                mockMvc.perform(get("/api/tasks/dashboard")
                                .header("Authorization", "Bearer " + userToken))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalTasks").value(4))
                        .andExpect(jsonPath("$.countByStatus['TODO']").value(1))
                        .andExpect(jsonPath("$.countByStatus['IN_PROGRESS']").value(1))
                        .andExpect(jsonPath("$.countByStatus['DONE']").value(1))
                        .andExpect(jsonPath("$.countByStatus['CANCELLED']").value(1))
                        .andExpect(jsonPath("$.countByPriority['HIGH']").value(2))
                        .andExpect(jsonPath("$.countByPriority['MEDIUM']").value(1))
                        .andExpect(jsonPath("$.countByPriority['LOW']").value(1))
                        .andExpect(jsonPath("$.overdueCount").value(0))
                        .andExpect(jsonPath("$.dueSoonCount").value(1));
            }

            @Test
            @DisplayName("should return zeroed metrics when user has no tasks")
            void shouldReturnZeroedMetricsWhenUserHasNoTasks() throws Exception {
                mockMvc.perform(get("/api/tasks/dashboard")
                                .header("Authorization", "Bearer " + userToken))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalTasks").value(0))
                        .andExpect(jsonPath("$.countByStatus").isEmpty())
                        .andExpect(jsonPath("$.countByPriority").isEmpty())
                        .andExpect(jsonPath("$.overdueCount").value(0))
                        .andExpect(jsonPath("$.dueSoonCount").value(0));
            }

            @Test
            @DisplayName("should only reflect the requesting user's own tasks (isolation)")
            void shouldOnlyReflectTheRequestingUsersOwnTasks() throws Exception {
                LocalDateTime now = LocalDateTime.now();
                String dueDate1 = now.plusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                String dueDate2 = now.plusDays(31).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                // Usuário A cria tarefas
                createTask("User A Task 1", Priority.HIGH, dueDate1);
                createTask("User A Task 2", Priority.MEDIUM, dueDate2);

                // Usuário B, recém-criado, sem nenhuma tarefa
                var secondUser = aSecondUser();
                userJpaRepository.save(secondUser);
                String secondUserToken = createAuthIdentityAndLogin(secondUser);

                // CRITICAL: usuário B vê métricas zeradas, mesmo com tarefas de A no banco
                mockMvc.perform(get("/api/tasks/dashboard")
                                .header("Authorization", "Bearer " + secondUserToken))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalTasks").value(0))
                        .andExpect(jsonPath("$.countByStatus").isEmpty())
                        .andExpect(jsonPath("$.countByPriority").isEmpty())
                        .andExpect(jsonPath("$.overdueCount").value(0))
                        .andExpect(jsonPath("$.dueSoonCount").value(0));

                // Confirma que o dashboard de A continua refletindo suas próprias tarefas
                mockMvc.perform(get("/api/tasks/dashboard")
                                .header("Authorization", "Bearer " + userToken))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalTasks").value(2));
            }

            @Test
            @DisplayName("should not collide with the /{taskId} route, resolving both endpoints correctly")
            void shouldNotCollideWithTheTaskIdRouteResolvingBothEndpointsCorrectly() throws Exception {
                LocalDateTime now = LocalDateTime.now();
                String dueDate = now.plusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                UUID taskId = createTask("Real Task", Priority.HIGH, dueDate);

                mockMvc.perform(get("/api/tasks/dashboard")
                                .header("Authorization", "Bearer " + userToken))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.totalTasks").value(1));

                mockMvc.perform(get("/api/tasks/{taskId}", taskId)
                                .header("Authorization", "Bearer " + userToken))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(taskId.toString()))
                        .andExpect(jsonPath("$.title").value("Real Task"));
            }
        }

        @Nested
        @DisplayName("WithError")
        class WithError {

            @Test
            @DisplayName("should return unauthorized when no token is provided")
            void shouldReturnUnauthorizedWhenNoTokenIsProvided() throws Exception {
                mockMvc.perform(get("/api/tasks/dashboard"))
                        .andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.title").value("Unauthorized"));
            }
        }

        private UUID createTask(String title, Priority priority, String dueDate) throws Exception {
            String response = mockMvc.perform(post("/api/tasks")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"title":"%s","description":"desc","priority":"%s","dueDate":"%s"}
                                    """.formatted(title, priority, dueDate)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            return objectMapper.readValue(response, TaskResponse.class).id();
        }

        private void changeStatus(UUID taskId, String status) throws Exception {
            mockMvc.perform(patch("/api/tasks/{taskId}/status", taskId)
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"status":"%s"}
                                    """.formatted(status)))
                    .andExpect(status().isOk());
        }
    }
}

