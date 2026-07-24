package br.com.forjacode.taskmanager.adapters.output.persistence.exception;

public class EmailAlreadyInUseException extends RuntimeException {
    public EmailAlreadyInUseException(String message) {
        super(message);
    }
}
