package br.com.forjacode.taskmanager.domain.exception;

public class InvalidGoogleTokenException extends RuntimeException {
    public InvalidGoogleTokenException() {
        super("Invalid or expired Google token");
    }
}
