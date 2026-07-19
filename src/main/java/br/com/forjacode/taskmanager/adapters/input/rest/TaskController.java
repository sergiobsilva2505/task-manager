package br.com.forjacode.taskmanager.adapters.input.rest;

import br.com.forjacode.taskmanager.adapters.input.rest.dto.CreateTaskRequest;
import br.com.forjacode.taskmanager.adapters.input.rest.dto.TaskResponse;
import br.com.forjacode.taskmanager.adapters.input.rest.mapper.TaskRestMapper;
import br.com.forjacode.taskmanager.application.ports.input.CreateTaskUseCase;
import br.com.forjacode.taskmanager.application.ports.input.GetTaskByIdUseCase;
import br.com.forjacode.taskmanager.application.ports.input.command.CreateTaskCommand;
import br.com.forjacode.taskmanager.domain.model.Task;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class TaskController {

    private final TaskRestMapper mapper;
    private final CreateTaskUseCase createTaskUseCase;
    private final GetTaskByIdUseCase getTaskByIdUseCase;

    public TaskController(TaskRestMapper mapper, CreateTaskUseCase createTaskUseCase,
            GetTaskByIdUseCase getTaskByIdUseCase) {
        this.mapper = mapper;
        this.createTaskUseCase = createTaskUseCase;
        this.getTaskByIdUseCase = getTaskByIdUseCase;
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
}
