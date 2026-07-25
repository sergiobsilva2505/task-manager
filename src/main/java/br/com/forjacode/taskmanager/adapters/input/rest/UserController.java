package br.com.forjacode.taskmanager.adapters.input.rest;

import br.com.forjacode.taskmanager.adapters.input.rest.dto.CreateUserRequest;
import br.com.forjacode.taskmanager.adapters.input.rest.dto.UserResponse;
import br.com.forjacode.taskmanager.adapters.input.rest.mapper.UserRestMapper;
import br.com.forjacode.taskmanager.application.ports.input.RegisterUserUseCase;
import br.com.forjacode.taskmanager.application.ports.input.command.RegisterUserCommand;
import br.com.forjacode.taskmanager.domain.model.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final UserRestMapper mapper;

    public UserController(RegisterUserUseCase registerUserUseCase, UserRestMapper mapper) {
        this.registerUserUseCase = registerUserUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid CreateUserRequest request) {
        RegisterUserCommand userCommand = mapper.toCommand(request);
        User user = registerUserUseCase.execute(userCommand);
        URI uri = URI.create("/api/users/%s".formatted(user.getId()));
        UserResponse response = mapper.toResponse(user);
        return ResponseEntity.created(uri).body(response);
    }
}
