package br.com.forjacode.taskmanager.adapters.input.rest.mapper;

import br.com.forjacode.taskmanager.adapters.input.rest.dto.CreateUserRequest;
import br.com.forjacode.taskmanager.adapters.input.rest.dto.UserResponse;
import br.com.forjacode.taskmanager.application.ports.input.command.RegisterUserCommand;
import br.com.forjacode.taskmanager.domain.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserRestMapper {

    RegisterUserCommand toCommand(CreateUserRequest request);

    UserResponse toResponse(User user);
}
