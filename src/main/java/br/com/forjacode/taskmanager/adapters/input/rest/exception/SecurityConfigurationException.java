package br.com.forjacode.taskmanager.adapters.input.rest.exception;

public class SecurityConfigurationException extends RuntimeException {
    public SecurityConfigurationException(String message) {
        super(message);
    }

    public SecurityConfigurationException(String message, String cause) {
        super(message + ": " + cause);
    }
}
