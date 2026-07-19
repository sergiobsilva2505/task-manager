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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

}