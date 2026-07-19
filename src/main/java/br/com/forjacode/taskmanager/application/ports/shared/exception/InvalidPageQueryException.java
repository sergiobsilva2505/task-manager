package br.com.forjacode.taskmanager.application.ports.shared.exception;

public class InvalidPageQueryException extends RuntimeException {
    public InvalidPageQueryException(String message) {
        super(message);
    }
}

