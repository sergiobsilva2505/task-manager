package br.com.forjacode.taskmanager.domain.exception;

public class MissingCurrentUserException extends RuntimeException {
    public MissingCurrentUserException(String message) {
        super(message);
    }
}
