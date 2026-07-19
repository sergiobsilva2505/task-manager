package br.com.forjacode.taskmanager.adapters.input.rest.exception;

import br.com.forjacode.taskmanager.domain.exception.InvalidInputException;
import br.com.forjacode.taskmanager.domain.exception.InvalidStatusTransitionException;
import br.com.forjacode.taskmanager.domain.exception.MissingRequiredFieldException;
import br.com.forjacode.taskmanager.domain.exception.TaskNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            MissingRequiredFieldException.class,
            InvalidInputException.class,
            InvalidStatusTransitionException.class
    })
    public ProblemDetail handleDomainExceptions(RuntimeException ex, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle(resolveTitle(ex));
        problemDetail.setProperty("path", extractPath(request));
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "One or more fields are invalid");
        problemDetail.setTitle("Validation Failed");
        problemDetail.setProperty("path", extractPath(request));
        problemDetail.setProperty("errors", fieldErrors);
        return problemDetail;
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ProblemDetail handleTaskNotFoundException(TaskNotFoundException ex, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Task Not Found");
        problemDetail.setProperty("path", extractPath(request));
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setProperty("path", extractPath(request));
        return problemDetail;
    }

    private String resolveTitle(RuntimeException ex) {
        return switch (ex) {
            case MissingRequiredFieldException e -> "Missing Required Field";
            case InvalidInputException e -> "Invalid Input";
            case InvalidStatusTransitionException e -> "Invalid Status Transition";
            default -> "Bad Request";
        };
    }

    private String extractPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}