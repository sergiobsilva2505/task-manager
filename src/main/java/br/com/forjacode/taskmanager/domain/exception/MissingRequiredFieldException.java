package br.com.forjacode.taskmanager.domain.exception;

public class MissingRequiredFieldException extends RuntimeException {
    public MissingRequiredFieldException(String message) {
        super(message);
    }
}
