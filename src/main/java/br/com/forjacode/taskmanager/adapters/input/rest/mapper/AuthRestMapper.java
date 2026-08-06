package br.com.forjacode.taskmanager.adapters.input.rest.mapper;

import br.com.forjacode.taskmanager.adapters.input.rest.dto.GoogleLoginRequest;
import br.com.forjacode.taskmanager.adapters.input.rest.dto.LoginRequest;
import br.com.forjacode.taskmanager.adapters.input.rest.dto.LoginResponse;
import br.com.forjacode.taskmanager.application.ports.input.command.GoogleLoginCommand;
import br.com.forjacode.taskmanager.application.ports.input.command.LoginCommand;
import br.com.forjacode.taskmanager.application.ports.input.result.LoginResult;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthRestMapper {

    LoginCommand toCommand(LoginRequest request);

    LoginResponse toResponse(LoginResult result);

    GoogleLoginCommand toCommand(GoogleLoginRequest request);
}
