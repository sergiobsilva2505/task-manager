package br.com.forjacode.taskmanager.domain.exception;

public class InvalidAuthIdentityException extends RuntimeException {
    public InvalidAuthIdentityException(String message) {
        super(message);
    }
}
