package br.com.forjacode.taskmanager.adapters.input.rest;

import br.com.forjacode.taskmanager.adapters.input.rest.dto.ChangeTaskStatusRequest;
import br.com.forjacode.taskmanager.adapters.input.rest.dto.CreateTaskRequest;
import br.com.forjacode.taskmanager.adapters.input.rest.dto.PagedResponse;
import br.com.forjacode.taskmanager.adapters.input.rest.dto.TaskResponse;
import br.com.forjacode.taskmanager.adapters.input.rest.mapper.TaskRestMapper;
import br.com.forjacode.taskmanager.application.ports.input.ChangeTaskStatusUseCase;
import br.com.forjacode.taskmanager.application.ports.input.CreateTaskUseCase;
import br.com.forjacode.taskmanager.application.ports.input.GetTaskByIdUseCase;
import br.com.forjacode.taskmanager.application.ports.input.ListTasksUseCase;
import br.com.forjacode.taskmanager.application.ports.input.command.ChangeTaskStatusCommand;
import br.com.forjacode.taskmanager.application.ports.input.command.CreateTaskCommand;
import br.com.forjacode.taskmanager.application.ports.shared.PageQuery;
import br.com.forjacode.taskmanager.application.ports.shared.PagedResult;
import br.com.forjacode.taskmanager.application.ports.shared.SortDirection;
import br.com.forjacode.taskmanager.application.ports.shared.TaskSortField;
import br.com.forjacode.taskmanager.domain.model.Task;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class TaskController {

    private final TaskRestMapper mapper;
    private final CreateTaskUseCase createTaskUseCase;
    private final GetTaskByIdUseCase getTaskByIdUseCase;
    private final ListTasksUseCase listTasksUseCase;
    private final ChangeTaskStatusUseCase changeTaskStatusUseCase;

    public TaskController(TaskRestMapper mapper, CreateTaskUseCase createTaskUseCase,
            GetTaskByIdUseCase getTaskByIdUseCase, ListTasksUseCase listTasksUseCase,
            ChangeTaskStatusUseCase changeTaskStatusUseCase) {
        this.mapper = mapper;
        this.createTaskUseCase = createTaskUseCase;
        this.getTaskByIdUseCase = getTaskByIdUseCase;
        this.listTasksUseCase = listTasksUseCase;
        this.changeTaskStatusUseCase = changeTaskStatusUseCase;
    }

    @PostMapping("/tasks")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest createTaskRequest) {
        CreateTaskCommand command = mapper.toCommand(createTaskRequest);
        Task task = createTaskUseCase.execute(command);
        TaskResponse response = mapper.toResponse(task);
        URI location = URI.create("/api/tasks/%s".formatted(task.getId()));
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable UUID taskId) {
        Task task = getTaskByIdUseCase.execute(taskId);
        TaskResponse response = mapper.toResponse(task);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tasks")
    public ResponseEntity<PagedResponse<TaskResponse>> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) TaskSortField sortField,
            @RequestParam(required = false) SortDirection sortDirection) {

        TaskSortField resolvedSortField = sortField != null ? sortField : TaskSortField.DEFAULT;
        SortDirection resolvedSortDirection = sortDirection != null ? sortDirection : SortDirection.DESC;

        PageQuery query = new PageQuery(page, size, resolvedSortField, resolvedSortDirection);
        PagedResult<Task> pagedResult = listTasksUseCase.execute(query);
        PagedResponse<TaskResponse> response = mapper.toResponse(pagedResult);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/tasks/{taskId}/status")
    public ResponseEntity<TaskResponse> changeTaskStatus(@PathVariable UUID taskId,
            @RequestBody @Valid ChangeTaskStatusRequest changeTaskStatusRequest) {
        ChangeTaskStatusCommand command = mapper.toCommand(taskId, changeTaskStatusRequest);
        Task updatedTask = changeTaskStatusUseCase.execute(command);
        TaskResponse response = mapper.toResponse(updatedTask);
        return ResponseEntity.ok(response);
    }
}
