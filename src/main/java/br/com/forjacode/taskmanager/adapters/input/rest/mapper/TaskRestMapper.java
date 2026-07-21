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

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface TaskRestMapper {

    CreateTaskCommand toCommand(CreateTaskRequest createTaskRequest);

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

    ChangeTaskStatusCommand toCommand(UUID taskId, ChangeTaskStatusRequest changeTaskStatusRequest);
}
