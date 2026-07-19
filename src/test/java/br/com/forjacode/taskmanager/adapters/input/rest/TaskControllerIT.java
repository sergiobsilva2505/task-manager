package br.com.forjacode.taskmanager.adapters.input.rest;

import br.com.forjacode.taskmanager.AbstractIntegrationTest;
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

import static org.assertj.core.api.Assertions.assertThat;
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