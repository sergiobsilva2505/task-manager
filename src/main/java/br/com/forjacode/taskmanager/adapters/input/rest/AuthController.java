package br.com.forjacode.taskmanager.adapters.input.rest;

import br.com.forjacode.taskmanager.adapters.input.rest.dto.LoginRequest;
import br.com.forjacode.taskmanager.adapters.input.rest.dto.LoginResponse;
import br.com.forjacode.taskmanager.adapters.input.rest.mapper.AuthRestMapper;
import br.com.forjacode.taskmanager.application.ports.input.LoginUseCase;
import br.com.forjacode.taskmanager.application.ports.input.command.LoginCommand;
import br.com.forjacode.taskmanager.application.ports.input.result.LoginResult;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final AuthRestMapper mapper;

    public AuthController(LoginUseCase loginUseCase, AuthRestMapper mapper) {
        this.loginUseCase = loginUseCase;
        this.mapper = mapper;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginCommand command = mapper.toCommand(request);
        LoginResult result = loginUseCase.execute(command);
        LoginResponse response = mapper.toResponse(result);
        return ResponseEntity.ok(response);
    }
}
