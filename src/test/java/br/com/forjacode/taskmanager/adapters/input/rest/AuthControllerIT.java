package br.com.forjacode.taskmanager.adapters.input.rest;

import br.com.forjacode.taskmanager.IntegrationTestSupport;
import br.com.forjacode.taskmanager.adapters.output.persistence.AuthIdentityJpaEntity;
import br.com.forjacode.taskmanager.adapters.output.persistence.AuthIdentityJpaRepository;
import br.com.forjacode.taskmanager.adapters.output.persistence.UserJpaEntity;
import br.com.forjacode.taskmanager.adapters.output.persistence.UserJpaRepository;
import br.com.forjacode.taskmanager.application.ports.output.GoogleTokenVerifierPort;
import br.com.forjacode.taskmanager.application.ports.output.GoogleUserInfo;
import br.com.forjacode.taskmanager.domain.model.enums.AuthProvider;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static br.com.forjacode.taskmanager.testsuport.UserJpaEntityBuilder.anUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIT extends IntegrationTestSupport {

    private static final String DEFAULT_PASSWORD = "SenhaForte123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private AuthIdentityJpaRepository authIdentityJpaRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @MockitoBean
    private GoogleTokenVerifierPort googleTokenVerifierPort;

    @BeforeEach
    void setUp() {
        cleanDatabase();
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    private void cleanDatabase() {
        authIdentityJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
    }

    private UserJpaEntity createLocalUser() {
        UserJpaEntity user = anUser();
        userJpaRepository.save(user);
        authIdentityJpaRepository.save(new AuthIdentityJpaEntity(
                UUID.randomUUID(), user.getId(), AuthProvider.LOCAL,
                passwordEncoder.encode(DEFAULT_PASSWORD), null, Instant.now()));
        return user;
    }

    @Nested
    @DisplayName("Login")
    class Login {

        @Nested
        @DisplayName("Success")
        class Success {

            @Test
            @DisplayName("should return token when credentials are valid")
            void shouldReturnTokenWhenCredentialsAreValid() throws Exception {
                UserJpaEntity user = createLocalUser();

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"email":"%s","password":"%s"}
                                        """.formatted(user.getEmail(), DEFAULT_PASSWORD)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.token").isNotEmpty())
                        .andExpect(jsonPath("$.expiresAt").isNotEmpty())
                        .andExpect(jsonPath("$.userId").value(user.getId().toString()));
            }
        }

        @Nested
        @DisplayName("WithError")
        class WithError {

            @Test
            @DisplayName("should return unauthorized when email does not exist")
            void shouldReturnUnauthorizedWhenEmailDoesNotExist() throws Exception {
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"email":"unknown@example.com","password":"%s"}
                                        """.formatted(DEFAULT_PASSWORD)))
                        .andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.title").value("Invalid Credentials"));
            }

            @Test
            @DisplayName("should return unauthorized when password is incorrect")
            void shouldReturnUnauthorizedWhenPasswordIsIncorrect() throws Exception {
                UserJpaEntity user = createLocalUser();

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"email":"%s","password":"WrongPassword123!"}
                                        """.formatted(user.getEmail())))
                        .andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.title").value("Invalid Credentials"));
            }

            @Test
            @DisplayName("should return bad request when email is blank")
            void shouldReturnBadRequestWhenEmailIsBlank() throws Exception {
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"email":"","password":"%s"}
                                        """.formatted(DEFAULT_PASSWORD)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Validation Failed"))
                        .andExpect(jsonPath("$.errors.email").exists());
            }

            @Test
            @DisplayName("should return bad request when email format is invalid")
            void shouldReturnBadRequestWhenEmailFormatIsInvalid() throws Exception {
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"email":"invalid-email","password":"%s"}
                                        """.formatted(DEFAULT_PASSWORD)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Validation Failed"))
                        .andExpect(jsonPath("$.errors.email").exists());
            }

            @Test
            @DisplayName("should return bad request when password is blank")
            void shouldReturnBadRequestWhenPasswordIsBlank() throws Exception {
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"email":"john.doe@example.com","password":""}
                                        """))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Validation Failed"))
                        .andExpect(jsonPath("$.errors.password").exists());
            }
        }
    }

    @Nested
    @DisplayName("Google login")
    class GoogleLogin {

        @Nested
        @DisplayName("Success")
        class Success {

            @Test
            @DisplayName("should create new user and return token when google user does not exist yet")
            void shouldCreateNewUserAndReturnTokenWhenGoogleUserDoesNotExistYet() throws Exception {
                when(googleTokenVerifierPort.verify("valid-id-token")).thenReturn(Optional.of(
                        new GoogleUserInfo("newuser@example.com", "New User", "google-sub-123")));

                mockMvc.perform(post("/api/auth/google")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"idToken":"valid-id-token"}
                                        """))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.token").isNotEmpty())
                        .andExpect(jsonPath("$.expiresAt").isNotEmpty())
                        .andExpect(jsonPath("$.userId").isNotEmpty());

                assertThat(userJpaRepository.findByEmail("newuser@example.com")).isPresent();
                UUID createdUserId = userJpaRepository.findByEmail("newuser@example.com").orElseThrow().getId();
                assertThat(authIdentityJpaRepository.findByProviderAndProviderUserId(AuthProvider.GOOGLE, "google-sub-123"))
                        .hasValueSatisfying(authIdentity -> assertThat(authIdentity.getUserId()).isEqualTo(createdUserId));
            }

            @Test
            @DisplayName("should return token for existing user already linked to google identity")
            void shouldReturnTokenForExistingUserAlreadyLinkedToGoogleIdentity() throws Exception {
                UserJpaEntity user = anUser();
                userJpaRepository.save(user);
                authIdentityJpaRepository.save(new AuthIdentityJpaEntity(
                        UUID.randomUUID(), user.getId(), AuthProvider.GOOGLE, null, "google-sub-999", Instant.now()));

                when(googleTokenVerifierPort.verify("valid-id-token")).thenReturn(Optional.of(
                        new GoogleUserInfo(user.getEmail(), user.getName(), "google-sub-999")));

                mockMvc.perform(post("/api/auth/google")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"idToken":"valid-id-token"}
                                        """))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.userId").value(user.getId().toString()));

                assertThat(userJpaRepository.count()).isEqualTo(1);
            }
        }

        @Nested
        @DisplayName("WithError")
        class WithError {

            @Test
            @DisplayName("should return unauthorized when google token is invalid")
            void shouldReturnUnauthorizedWhenGoogleTokenIsInvalid() throws Exception {
                when(googleTokenVerifierPort.verify("invalid-id-token")).thenReturn(Optional.empty());

                mockMvc.perform(post("/api/auth/google")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"idToken":"invalid-id-token"}
                                        """))
                        .andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.title").value("Invalid Google Token"))
                        .andExpect(jsonPath("$.detail").value("Invalid or expired Google token"));
            }

            @Test
            @DisplayName("should return bad request when idToken is blank")
            void shouldReturnBadRequestWhenIdTokenIsBlank() throws Exception {
                mockMvc.perform(post("/api/auth/google")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"idToken":""}
                                        """))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Validation Failed"))
                        .andExpect(jsonPath("$.errors.idToken").exists());
            }
        }
    }
}