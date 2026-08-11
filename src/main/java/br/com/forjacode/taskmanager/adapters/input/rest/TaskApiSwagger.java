package br.com.forjacode.taskmanager.adapters.input.rest;

import br.com.forjacode.taskmanager.adapters.input.rest.dto.ChangeTaskStatusRequest;
import br.com.forjacode.taskmanager.adapters.input.rest.dto.CreateTaskRequest;
import br.com.forjacode.taskmanager.adapters.input.rest.dto.DashboardResponse;
import br.com.forjacode.taskmanager.adapters.input.rest.dto.PagedResponse;
import br.com.forjacode.taskmanager.adapters.input.rest.dto.TaskResponse;
import br.com.forjacode.taskmanager.application.ports.shared.SortDirection;
import br.com.forjacode.taskmanager.application.ports.shared.TaskSortField;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Tag(name = "Tasks", description = "Operações de gerenciamento de tarefas")
public interface TaskApiSwagger {

    @Operation(
            summary = "Cria uma nova tarefa",
            description = "Cria uma tarefa pertencente ao usuário autenticado (identificado pelo token)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tarefa criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Campos obrigatórios ausentes, prazo inválido ou falha " +
                    "de validação de formato",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    ResponseEntity<TaskResponse> createTask(CreateTaskRequest createTaskRequest, UUID currentUserId);

    @Operation(
            summary = "Busca uma tarefa por ID",
            description = "Retorna a tarefa apenas se ela existir e pertencer ao usuário autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarefa encontrada"),
            @ApiResponse(responseCode = "404", description = "Tarefa não existe ou pertence a outro usuário (mesma " +
                    "resposta nos dois casos)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "400", description = "ID informado não é um UUID válido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    ResponseEntity<TaskResponse> getTaskById(
            @Parameter(description = "ID da tarefa") UUID taskId,
            UUID currentUserId);

    @Operation(
            summary = "Lista as tarefas do usuário, com paginação e ordenação",
            description = "Retorna apenas as tarefas pertencentes ao usuário autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "sortField/sortDirection fora da whitelist permitida",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    ResponseEntity<PagedResponse<TaskResponse>> listAll(
            @Parameter(description = "Número da página (a partir de 0)") int page,
            @Parameter(description = "Quantidade de itens por página") int size,
            @Parameter(description = "Campo de ordenação") TaskSortField sortField,
            @Parameter(description = "Direção da ordenação") SortDirection sortDirection,
            UUID currentUserId);

    @Operation(
            summary = "Altera o status de uma tarefa",
            description = "Só permite transições válidas na máquina de estados (TODO → IN_PROGRESS/CANCELLED, " +
                    "IN_PROGRESS → DONE/CANCELLED)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status alterado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Tarefa não existe ou pertence a outro usuário",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "400", description = "Transição inválida, status ausente ou valor de status " +
                    "desconhecido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    ResponseEntity<TaskResponse> changeTaskStatus(
            @Parameter(description = "ID da tarefa") UUID taskId,
            ChangeTaskStatusRequest changeTaskStatusRequest,
            UUID currentUserId);

    @Operation(
            summary = "Remove uma tarefa",
            description = "Operação idempotente: sempre retorna 204, independentemente de a tarefa existir, pertencer" +
                    " a outro usuário, ou já ter sido removida."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tarefa removida (ou já não existia/não era sua — " +
                    "resposta idêntica)"),
            @ApiResponse(responseCode = "400", description = "ID informado não é um UUID válido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    ResponseEntity<Void> deleteTask(
            @Parameter(description = "ID da tarefa") UUID taskId,
            UUID currentUserId);

    @Operation(
            summary = "Retorna métricas agregadas das tarefas do usuário",
            description = "Total de tarefas, contagem por status e prioridade, tarefas atrasadas e vencendo nos " +
                    "próximos 7 dias."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Métricas calculadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    ResponseEntity<DashboardResponse> getDashboard(UUID currentUserId);
}
