package br.com.forjacode.taskmanager.adapters.input.rest;

import br.com.forjacode.taskmanager.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("Create user")
    class CreateUser {

        @Nested
        @DisplayName("Success")
        class Success {

            @Test
            @DisplayName("should create user with valid request")
            void shouldCreateUserWithValidRequest() throws Exception {
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"name":"John Doe","email":"john.doe@example.com"}
                                        """))
                        .andExpect(status().isCreated())
                        .andExpect(header().exists("Location"))
                        .andExpect(jsonPath("$.id").isNotEmpty())
                        .andExpect(jsonPath("$.name").value("John Doe"))
                        .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                        .andExpect(jsonPath("$.createdAt").isNotEmpty());
            }

            @Test
            @DisplayName("should create user with name containing hyphens")
            void shouldCreateUserWithNameContainingHyphens() throws Exception {
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"name":"Mary-Jane","email":"mary@example.com"}
                                        """))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.name").value("Mary-Jane"));
            }

            @Test
            @DisplayName("should create user with name containing apostrophes")
            void shouldCreateUserWithNameContainingApostrophes() throws Exception {
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"name":"O'Connor","email":"oconnor@example.com"}
                                        """))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.name").value("O'Connor"));
            }

            @Test
            @DisplayName("should create user with name containing accented characters")
            void shouldCreateUserWithNameContainingAccentedCharacters() throws Exception {
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"name":"José García","email":"jose@example.com"}
                                        """))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.name").value("José García"));
            }

            @Test
            @DisplayName("should create user with minimum valid name length")
            void shouldCreateUserWithMinimumValidNameLength() throws Exception {
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"name":"Ana","email":"ana@example.com"}
                                        """))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.name").value("Ana"));
            }

            @Test
            @DisplayName("should create user with maximum valid name length")
            void shouldCreateUserWithMaximumValidNameLength() throws Exception {
                String longName = "A".repeat(160);
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"name":"%s","email":"user@example.com"}
                                        """.formatted(longName)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.name").value(longName));
            }

            @Test
            @DisplayName("should create multiple users with different emails")
            void shouldCreateMultipleUsersWithDifferentEmails() throws Exception {
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"name":"John Doe","email":"john@example.com"}
                                        """))
                        .andExpect(status().isCreated());

                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"name":"Jane Smith","email":"jane@example.com"}
                                        """))
                        .andExpect(status().isCreated());
            }
        }

        @Nested
        @DisplayName("WithError")
        class WithError {

            @Test
            @DisplayName("should return bad request when name is null")
            void shouldReturnBadRequestWhenNameIsNull() throws Exception {
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"name":null,"email":"email@example.com"}
                                        """))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Validation Failed"))
                        .andExpect(jsonPath("$.errors.name").exists());
            }

            @Test
            @DisplayName("should return bad request when name is blank")
            void shouldReturnBadRequestWhenNameIsBlank() throws Exception {
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"name":"   ","email":"email@example.com"}
                                        """))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Validation Failed"))
                        .andExpect(jsonPath("$.errors.name").exists());
            }

            @Test
            @DisplayName("should return bad request when name is empty")
            void shouldReturnBadRequestWhenNameIsEmpty() throws Exception {
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"name":"","email":"email@example.com"}
                                        """))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Validation Failed"))
                        .andExpect(jsonPath("$.errors.name").exists());
            }

            @Test
            @DisplayName("should return bad request when name is too short")
            void shouldReturnBadRequestWhenNameIsTooShort() throws Exception {
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"name":"Jo","email":"jo@example.com"}
                                        """))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Validation Failed"))
                        .andExpect(jsonPath("$.errors.name").exists());
            }

            @Test
            @DisplayName("should return bad request when name is too long")
            void shouldReturnBadRequestWhenNameIsTooLong() throws Exception {
                String longName = "A".repeat(161);
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"name":"%s","email":"user@example.com"}
                                        """.formatted(longName)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Validation Failed"))
                        .andExpect(jsonPath("$.errors.name").exists());
            }

            @Test
            @DisplayName("should return bad request when name contains numbers")
            void shouldReturnBadRequestWhenNameContainsNumbers() throws Exception {
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"name":"John123","email":"john@example.com"}
                                        """))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Validation Failed"))
                        .andExpect(jsonPath("$.errors.name").exists())
                        .andExpect(jsonPath("$.errors.name").value(
                                containsString("letters, spaces, hyphens or apostrophes")));
            }

            @Test
            @DisplayName("should return bad request when name contains special characters")
            void shouldReturnBadRequestWhenNameContainsSpecialCharacters() throws Exception {
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"name":"John@Doe","email":"john@example.com"}
                                        """))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Validation Failed"))
                        .andExpect(jsonPath("$.errors.name").exists())
                        .andExpect(jsonPath("$.errors.name").value(
                                containsString("letters, spaces, hyphens or apostrophes")));
            }

            @Test
            @DisplayName("should return bad request when email is null")
            void shouldReturnBadRequestWhenEmailIsNull() throws Exception {
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"name":"John Doe","email":null}
                                        """))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Validation Failed"))
                        .andExpect(jsonPath("$.errors.email").exists());
            }

            @Test
            @DisplayName("should return bad request when email is blank")
            void shouldReturnBadRequestWhenEmailIsBlank() throws Exception {
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"name":"John Doe","email":"   "}
                                        """))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Validation Failed"))
                        .andExpect(jsonPath("$.errors.email").exists());
            }

            @Test
            @DisplayName("should return bad request when email is empty")
            void shouldReturnBadRequestWhenEmailIsEmpty() throws Exception {
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"name":"John Doe","email":""}
                                        """))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Validation Failed"))
                        .andExpect(jsonPath("$.errors.email").exists());
            }

            @Test
            @DisplayName("should return bad request when email format is invalid")
            void shouldReturnBadRequestWhenEmailFormatIsInvalid() throws Exception {
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"name":"John Doe","email":"invalid-email"}
                                        """))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Validation Failed"))
                        .andExpect(jsonPath("$.errors.email").exists());
            }

            @Test
            @DisplayName("should return bad request when both name and email are invalid")
            void shouldReturnBadRequestWhenBothNameAndEmailAreInvalid() throws Exception {
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"name":"Jo","email":"invalid"}
                                        """))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Validation Failed"))
                        .andExpect(jsonPath("$.errors.name").exists())
                        .andExpect(jsonPath("$.errors.email").exists());
            }

            @Test
            @DisplayName("should return bad request when request body is missing")
            void shouldReturnBadRequestWhenRequestBodyIsMissing() throws Exception {
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(""))
                        .andExpect(status().isBadRequest());
            }
        }
    }
}
