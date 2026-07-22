package br.com.forjacode.taskmanager.adapters.input.rest;

import br.com.forjacode.taskmanager.AbstractIntegrationTest;
import br.com.forjacode.taskmanager.adapters.input.rest.dto.TaskResponse;
import br.com.forjacode.taskmanager.adapters.output.persistence.TaskJpaRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
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
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        taskJpaRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        taskJpaRepository.deleteAll();
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
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task","description":"desc","priority":"HIGH","dueDate":"2020-01-01T10:00:00"}
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
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"My Task","description":"Task description","priority":"HIGH","dueDate":"2026-08-01T10:00:00"}
                                        """))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                TaskResponse createdTask = objectMapper.readValue(createResponse, TaskResponse.class);

                mockMvc.perform(get("/api/tasks/{taskId}", createdTask.id()))
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

                mockMvc.perform(get("/api/tasks/{taskId}", nonExistentId))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.title").value("Task Not Found"))
                        .andExpect(jsonPath("$.detail").value("Task with ID %s not found".formatted(nonExistentId)));
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
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task B","description":"desc","priority":"HIGH","dueDate":"2026-08-01T10:00:00"}
                                        """))
                        .andExpect(status().isCreated());

                mockMvc.perform(post("/api/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task A","description":"desc","priority":"MEDIUM","dueDate":"2026-08-02T10:00:00"}
                                        """))
                        .andExpect(status().isCreated());

                mockMvc.perform(get("/api/tasks")
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
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task 1","description":"desc","priority":"HIGH","dueDate":"2026-08-01T10:00:00"}
                                        """))
                        .andExpect(status().isCreated());

                mockMvc.perform(post("/api/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task 2","description":"desc","priority":"LOW","dueDate":"2026-08-03T10:00:00"}
                                        """))
                        .andExpect(status().isCreated());

                mockMvc.perform(get("/api/tasks"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.page").value(0))
                        .andExpect(jsonPath("$.size").value(20))
                        .andExpect(jsonPath("$.totalElements").value(2))
                        .andExpect(jsonPath("$.content.length()").value(2));
            }
        }

        @Nested
        @DisplayName("WithError")
        class WithError {

            @Test
            @DisplayName("should return bad request when sort field is invalid")
            void shouldReturnBadRequestWhenSortFieldIsInvalid() throws Exception {
                mockMvc.perform(get("/api/tasks")
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
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task only","description":"desc","priority":"HIGH","dueDate":"2026-08-01T10:00:00"}
                                        """))
                        .andExpect(status().isCreated());

                mockMvc.perform(get("/api/tasks")
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
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task","description":"desc","priority":"HIGH","dueDate":"2026-08-01T10:00:00"}
                                        """))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                TaskResponse createdTask = objectMapper.readValue(createResponse, TaskResponse.class);

                mockMvc.perform(patch("/api/tasks/{taskId}/status", createdTask.id())
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
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"status":"IN_PROGRESS"}
                                        """))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.title").value("Task Not Found"))
                        .andExpect(jsonPath("$.detail").value("Task with ID %s not found".formatted(nonExistentId)));
            }

            @Test
            @DisplayName("should return bad request when transition is invalid")
            void shouldReturnBadRequestWhenTransitionIsInvalid() throws Exception {
                String createResponse = mockMvc.perform(post("/api/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task","description":"desc","priority":"HIGH","dueDate":"2026-08-01T10:00:00"}
                                        """))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                TaskResponse createdTask = objectMapper.readValue(createResponse, TaskResponse.class);

                mockMvc.perform(patch("/api/tasks/{taskId}/status", createdTask.id())
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
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task","description":"desc","priority":"HIGH","dueDate":"2026-08-01T10:00:00"}
                                        """))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                TaskResponse createdTask = objectMapper.readValue(createResponse, TaskResponse.class);

                mockMvc.perform(patch("/api/tasks/{taskId}/status", createdTask.id())
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
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"title":"Task","description":"desc","priority":"HIGH","dueDate":"2026-08-01T10:00:00"}
                                        """))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                TaskResponse createdTask = objectMapper.readValue(createResponse, TaskResponse.class);

                mockMvc.perform(patch("/api/tasks/{taskId}/status", createdTask.id())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"status":"NAOEXISTE"}
                                        """))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Malformed JSON"));
            }
        }
    }
}