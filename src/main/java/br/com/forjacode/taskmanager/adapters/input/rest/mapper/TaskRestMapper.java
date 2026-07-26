package br.com.forjacode.taskmanager.adapters.input.rest.mapper;

import br.com.forjacode.taskmanager.adapters.input.rest.dto.ChangeTaskStatusRequest;
import br.com.forjacode.taskmanager.adapters.input.rest.dto.CreateTaskRequest;
import br.com.forjacode.taskmanager.adapters.input.rest.dto.PagedResponse;
import br.com.forjacode.taskmanager.adapters.input.rest.dto.TaskResponse;
import br.com.forjacode.taskmanager.application.ports.input.command.ChangeTaskStatusCommand;
import br.com.forjacode.taskmanager.application.ports.input.command.CreateTaskCommand;
import br.com.forjacode.taskmanager.application.ports.shared.PagedResult;
import br.com.forjacode.taskmanager.domain.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface TaskRestMapper {

    @Mapping(source = "currentUserId", target = "ownerId")
    CreateTaskCommand toCommand(CreateTaskRequest createTaskRequest, UUID currentUserId);


    TaskResponse toResponse(Task task);

    List<TaskResponse> toResponse(List<Task> tasks);

    default PagedResponse<TaskResponse> toResponse(PagedResult<Task> pagedResult) {
        return new PagedResponse<>(
                toResponse(pagedResult.content()), // agora compila
                pagedResult.page(),
                pagedResult.size(),
                pagedResult.totalElements(),
                pagedResult.totalPages()
        );
    }

    @Mapping(source = "currentUserId", target = "ownerId")
    @Mapping(source = "changeTaskStatusRequest.status", target = "newStatus")
    ChangeTaskStatusCommand toCommand(UUID taskId, ChangeTaskStatusRequest changeTaskStatusRequest, UUID currentUserId);
}
