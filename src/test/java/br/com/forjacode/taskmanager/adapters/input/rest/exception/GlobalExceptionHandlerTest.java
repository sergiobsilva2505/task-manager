package br.com.forjacode.taskmanager.adapters.input.rest.exception;

import br.com.forjacode.taskmanager.domain.exception.EmailAlreadyInUseException;
import br.com.forjacode.taskmanager.domain.exception.InvalidCredentialsException;
import br.com.forjacode.taskmanager.domain.exception.InvalidGoogleTokenException;
import br.com.forjacode.taskmanager.domain.exception.InvalidInputException;
import br.com.forjacode.taskmanager.domain.exception.InvalidStatusTransitionException;
import br.com.forjacode.taskmanager.domain.exception.MissingCurrentUserException;
import br.com.forjacode.taskmanager.domain.exception.MissingRequiredFieldException;
import br.com.forjacode.taskmanager.domain.exception.TaskNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private WebRequest webRequest;

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Nested
    @DisplayName("Handle domain exceptions")
    class HandleDomainExceptions {

        @Nested
        @DisplayName("Success")
        class Success {

            @Test
            @DisplayName("should return bad request when exception is MissingRequiredFieldException")
            void shouldReturnBadRequestWhenExceptionIsMissingRequiredFieldException() {
                when(webRequest.getDescription(false)).thenReturn("uri=/api/tasks");
                MissingRequiredFieldException ex = new MissingRequiredFieldException("Name cannot be null or blank");

                ProblemDetail problemDetail = globalExceptionHandler.handleDomainExceptions(ex, webRequest);

                assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
                assertThat(problemDetail.getDetail()).isEqualTo("Name cannot be null or blank");
                assertThat(problemDetail.getTitle()).isEqualTo("Missing Required Field");
                assertThat(problemDetail.getProperties()).containsEntry("path", "/api/tasks");
            }

            @Test
            @DisplayName("should return bad request when exception is InvalidInputException")
            void shouldReturnBadRequestWhenExceptionIsInvalidInputException() {
                when(webRequest.getDescription(false)).thenReturn("uri=/api/users");
                InvalidInputException ex = new InvalidInputException("Name must be between 3 and 160 characters");

                ProblemDetail problemDetail = globalExceptionHandler.handleDomainExceptions(ex, webRequest);

                assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
                assertThat(problemDetail.getDetail()).isEqualTo("Name must be between 3 and 160 characters");
                assertThat(problemDetail.getTitle()).isEqualTo("Invalid Input");
                assertThat(problemDetail.getProperties()).containsEntry("path", "/api/users");
            }

            @Test
            @DisplayName("should return bad request when exception is InvalidStatusTransitionException")
            void shouldReturnBadRequestWhenExceptionIsInvalidStatusTransitionException() {
                when(webRequest.getDescription(false)).thenReturn("uri=/api/tasks/1/status");
                InvalidStatusTransitionException ex =
                        new InvalidStatusTransitionException("Cannot change status from TODO to DONE");

                ProblemDetail problemDetail = globalExceptionHandler.handleDomainExceptions(ex, webRequest);

                assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
                assertThat(problemDetail.getDetail()).isEqualTo("Cannot change status from TODO to DONE");
                assertThat(problemDetail.getTitle()).isEqualTo("Invalid Status Transition");
                assertThat(problemDetail.getProperties()).containsEntry("path", "/api/tasks/1/status");
            }

            @Test
            @DisplayName("should return bad request when exception is MissingCurrentUserException")
            void shouldReturnBadRequestWhenExceptionIsMissingCurrentUserException() {
                when(webRequest.getDescription(false)).thenReturn("uri=/api/tasks");
                MissingCurrentUserException ex = new MissingCurrentUserException("Current user is missing");

                ProblemDetail problemDetail = globalExceptionHandler.handleDomainExceptions(ex, webRequest);

                assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
                assertThat(problemDetail.getDetail()).isEqualTo("Current user is missing");
                assertThat(problemDetail.getTitle()).isEqualTo("Missing Current User");
                assertThat(problemDetail.getProperties()).containsEntry("path", "/api/tasks");
            }

            @Test
            @DisplayName("should return generic bad request title when exception type is not mapped")
            void shouldReturnGenericBadRequestTitleWhenExceptionTypeIsNotMapped() {
                when(webRequest.getDescription(false)).thenReturn("uri=/api/tasks");
                RuntimeException ex = new RuntimeException("Some unmapped domain error");

                ProblemDetail problemDetail = globalExceptionHandler.handleDomainExceptions(ex, webRequest);

                assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
                assertThat(problemDetail.getTitle()).isEqualTo("Bad Request");
            }
        }
    }

    @Nested
    @DisplayName("Handle validation exceptions")
    class HandleValidationExceptions {

        @Nested
        @DisplayName("Success")
        class Success {

            @Test
            @DisplayName("should return bad request with field errors when validation fails")
            void shouldReturnBadRequestWithFieldErrorsWhenValidationFails() {
                when(webRequest.getDescription(false)).thenReturn("uri=/api/users");

                MethodArgumentNotValidException ex = mockMethodArgumentNotValidException(
                        new FieldError("request", "name", "must not be blank"),
                        new FieldError("request", "email", "must be a well-formed email address"));

                ProblemDetail problemDetail = globalExceptionHandler.handleValidationExceptions(ex, webRequest);

                assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
                assertThat(problemDetail.getDetail()).isEqualTo("One or more fields are invalid");
                assertThat(problemDetail.getTitle()).isEqualTo("Validation Failed");
                assertThat(problemDetail.getProperties()).containsEntry("path", "/api/users");

                assertThat(problemDetail.getProperties()).containsKey("errors");
                @SuppressWarnings({"unchecked", "DataFlowIssue"})
                var errors = (java.util.Map<String, String>) problemDetail.getProperties().get("errors");
                assertThat(errors)
                        .containsEntry("name", "must not be blank")
                        .containsEntry("email", "must be a well-formed email address");
            }

            private MethodArgumentNotValidException mockMethodArgumentNotValidException(FieldError... fieldErrors) {
                MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
                BindingResult bindingResult = mock(BindingResult.class);
                when(ex.getBindingResult()).thenReturn(bindingResult);
                when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldErrors));
                return ex;
            }
        }
    }

    @Nested
    @DisplayName("Handle task not found exception")
    class HandleTaskNotFoundException {

        @Nested
        @DisplayName("Success")
        class Success {

            @Test
            @DisplayName("should return not found when task does not exist")
            void shouldReturnNotFoundWhenTaskDoesNotExist() {
                when(webRequest.getDescription(false)).thenReturn("uri=/api/tasks/123");
                TaskNotFoundException ex = new TaskNotFoundException("Task with ID 123 not found");

                ProblemDetail problemDetail = globalExceptionHandler.handleTaskNotFoundException(ex, webRequest);

                assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
                assertThat(problemDetail.getDetail()).isEqualTo("Task with ID 123 not found");
                assertThat(problemDetail.getTitle()).isEqualTo("Task Not Found");
                assertThat(problemDetail.getProperties()).containsEntry("path", "/api/tasks/123");
            }
        }
    }

    @Nested
    @DisplayName("Handle generic exception")
    class HandleGenericException {

        @Nested
        @DisplayName("Success")
        class Success {

            @Test
            @DisplayName("should return internal server error when exception is unexpected")
            void shouldReturnInternalServerErrorWhenExceptionIsUnexpected() {
                when(webRequest.getDescription(false)).thenReturn("uri=/api/tasks");
                Exception ex = new RuntimeException("Something exploded");

                ProblemDetail problemDetail = globalExceptionHandler.handleGenericException(ex, webRequest);

                assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
                assertThat(problemDetail.getDetail()).isEqualTo("An unexpected error occurred");
                assertThat(problemDetail.getTitle()).isEqualTo("Internal Server Error");
                assertThat(problemDetail.getProperties()).containsEntry("path", "/api/tasks");
            }
        }
    }

    @Nested
    @DisplayName("Handle method argument type mismatch exception")
    class HandleMethodArgumentTypeMismatchException {

        @Nested
        @DisplayName("Success")
        class Success {

            @Test
            @DisplayName("should return bad request with expected type when type is known")
            void shouldReturnBadRequestWithExpectedTypeWhenTypeIsKnown() {
                when(webRequest.getDescription(false)).thenReturn("uri=/api/tasks");
                MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
                when(ex.getValue()).thenReturn("INVALID_SORT");
                when(ex.getName()).thenReturn("sortField");
                doReturn(String.class).when(ex).getRequiredType();

                ProblemDetail problemDetail =
                        globalExceptionHandler.handleMethodArgumentTypeMismatchException(ex, webRequest);

                assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
                assertThat(problemDetail.getDetail())
                        .isEqualTo("Invalid value 'INVALID_SORT' for parameter 'sortField'. Expected type: String");
                assertThat(problemDetail.getTitle()).isEqualTo("Invalid Parameter Type");
                assertThat(problemDetail.getProperties()).containsEntry("path", "/api/tasks");
            }
        }

        @Nested
        @DisplayName("WithError")
        class WithError {

            @Test
            @DisplayName("should return unknown expected type when required type is null")
            void shouldReturnUnknownExpectedTypeWhenRequiredTypeIsNull() {
                when(webRequest.getDescription(false)).thenReturn("uri=/api/tasks");
                MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
                when(ex.getValue()).thenReturn("abc");
                when(ex.getName()).thenReturn("page");
                doReturn(null).when(ex).getRequiredType();

                ProblemDetail problemDetail =
                        globalExceptionHandler.handleMethodArgumentTypeMismatchException(ex, webRequest);

                assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
                assertThat(problemDetail.getDetail())
                        .isEqualTo("Invalid value 'abc' for parameter 'page'. Expected type: unknown");
                assertThat(problemDetail.getTitle()).isEqualTo("Invalid Parameter Type");
            }
        }
    }

    @Nested
    @DisplayName("Handle http message not readable exception")
    class HandleHttpMessageNotReadableException {

        @Nested
        @DisplayName("Success")
        class Success {

            @Test
            @DisplayName("should return bad request when JSON is malformed")
            void shouldReturnBadRequestWhenJsonIsMalformed() {
                when(webRequest.getDescription(false)).thenReturn("uri=/api/tasks");
                HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);

                ProblemDetail problemDetail =
                        globalExceptionHandler.handleHttpMessageNotReadableException(ex, webRequest);

                assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
                assertThat(problemDetail.getDetail()).isEqualTo("Malformed JSON request");
                assertThat(problemDetail.getTitle()).isEqualTo("Malformed JSON");
                assertThat(problemDetail.getProperties()).containsEntry("path", "/api/tasks");
            }
        }
    }

    @Nested
    @DisplayName("Handle email already in use exception")
    class HandleEmailAlreadyInUseException {

        @Nested
        @DisplayName("Success")
        class Success {

            @Test
            @DisplayName("should return conflict when email is already in use")
            void shouldReturnConflictWhenEmailIsAlreadyInUse() {
                when(webRequest.getDescription(false)).thenReturn("uri=/api/users");
                EmailAlreadyInUseException ex =
                        new EmailAlreadyInUseException("Email john.doe@example.com is already in use");

                ProblemDetail problemDetail = globalExceptionHandler.handleEmailAlreadyInUseException(ex, webRequest);

                assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
                assertThat(problemDetail.getDetail()).isEqualTo("Email john.doe@example.com is already in use");
                assertThat(problemDetail.getTitle()).isEqualTo("Email Already In Use");
                assertThat(problemDetail.getProperties()).containsEntry("path", "/api/users");
            }
        }
    }

    @Nested
    @DisplayName("Handle invalid credentials exception")
    class HandleInvalidCredentialsException {

        @Nested
        @DisplayName("Success")
        class Success {

            @Test
            @DisplayName("should return unauthorized when credentials are invalid")
            void shouldReturnUnauthorizedWhenCredentialsAreInvalid() {
                when(webRequest.getDescription(false)).thenReturn("uri=/api/auth/login");
                InvalidCredentialsException ex = new InvalidCredentialsException();

                ProblemDetail problemDetail = globalExceptionHandler.handleInvalidCredentialsException(ex, webRequest);

                assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
                assertThat(problemDetail.getDetail()).isEqualTo("Invalid email or password");
                assertThat(problemDetail.getTitle()).isEqualTo("Invalid Credentials");
                assertThat(problemDetail.getProperties()).containsEntry("path", "/api/auth/login");
            }
        }
    }

    @Nested
    @DisplayName("Handle invalid google token exception")
    class HandleInvalidGoogleTokenException {

        @Nested
        @DisplayName("Success")
        class Success {

            @Test
            @DisplayName("should return unauthorized when google token is invalid or expired")
            void shouldReturnUnauthorizedWhenGoogleTokenIsInvalidOrExpired() {
                when(webRequest.getDescription(false)).thenReturn("uri=/api/auth/google");
                InvalidGoogleTokenException ex = new InvalidGoogleTokenException();

                ProblemDetail problemDetail =
                        globalExceptionHandler.handleInvalidGoogleTokenException(ex, webRequest);

                assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
                assertThat(problemDetail.getDetail()).isEqualTo("Invalid or expired Google token");
                assertThat(problemDetail.getTitle()).isEqualTo("Invalid Google Token");
                assertThat(problemDetail.getProperties()).containsEntry("path", "/api/auth/google");
            }
        }
    }
}