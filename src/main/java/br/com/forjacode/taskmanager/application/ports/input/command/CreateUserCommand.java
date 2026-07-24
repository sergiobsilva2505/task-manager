package br.com.forjacode.taskmanager.application.ports.input.command;

public record CreateUserCommand(String name, String email) {
}
