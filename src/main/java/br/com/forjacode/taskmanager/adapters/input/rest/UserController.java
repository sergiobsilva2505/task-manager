package br.com.forjacode.taskmanager.adapters.input.rest;

import br.com.forjacode.taskmanager.adapters.input.rest.dto.CreateUserRequest;
import br.com.forjacode.taskmanager.adapters.input.rest.dto.UserResponse;
import br.com.forjacode.taskmanager.adapters.input.rest.mapper.UserRestMapper;
import br.com.forjacode.taskmanager.application.ports.input.RegisterUserCase;
import br.com.forjacode.taskmanager.application.ports.input.command.CreateUserCommand;
import br.com.forjacode.taskmanager.domain.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final RegisterUserCase registerUserCase;
    private final UserRestMapper mapper;

    public UserController(RegisterUserCase registerUserCase, UserRestMapper mapper) {
        this.registerUserCase = registerUserCase;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(CreateUserRequest request) {
        CreateUserCommand userCommand = mapper.toCommand(request);
        User user = registerUserCase.execute(userCommand);
        URI uri = URI.create("/api/users/%s".formatted(user.getId()));
        UserResponse response = mapper.toResponse(user);
        return ResponseEntity.created(uri).body(response);
    }

}
