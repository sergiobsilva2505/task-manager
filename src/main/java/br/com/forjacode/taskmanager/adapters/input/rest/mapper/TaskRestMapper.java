package br.com.forjacode.taskmanager.adapters.input.rest.mapper;

import br.com.forjacode.taskmanager.adapters.input.rest.dto.ChangeTaskStatusRequest;
import br.com.forjacode.taskmanager.adapters.input.rest.dto.CreateTaskRequest;
import br.com.forjacode.taskmanager.adapters.input.rest.dto.DashboardResponse;
import br.com.forjacode.taskmanager.adapters.input.rest.dto.PagedResponse;
import br.com.forjacode.taskmanager.adapters.input.rest.dto.TaskResponse;
import br.com.forjacode.taskmanager.application.ports.input.command.ChangeTaskStatusCommand;
import br.com.forjacode.taskmanager.application.ports.input.command.CreateTaskCommand;
import br.com.forjacode.taskmanager.application.ports.input.result.DashboardResult;
import br.com.forjacode.taskmanager.application.ports.shared.PagedResult;
import br.com.forjacode.taskmanager.domain.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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

    default DashboardResponse toResponse(DashboardResult result) {
        return new DashboardResponse(
                result.totalTasks(),
                convertKeysToString(result.countByStatus()),
                convertKeysToString(result.countByPriority()),
                result.overdueCount(),
                result.dueSoonCount()
        );
    }

    private <K extends Enum<K>> Map<String, Long> convertKeysToString(Map<K, Long> source) {
        return source.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().name(), Map.Entry::getValue));
    }
}
