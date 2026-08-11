package br.com.forjacode.taskmanager.adapters.input.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

public record DashboardResponse(
        @Schema(description = "Total de tarefas do usuário")
        long totalTasks,
        @Schema(description = "Contagem de tarefas agrupadas por status")
        Map<String, Long> countByStatus,
        @Schema(description = "Contagem de tarefas agrupadas por prioridade")
        Map<String, Long> countByPriority,
        @Schema(description = "Quantidade de tarefas atrasadas")
        long overdueCount,
        @Schema(description = "Quantidade de tarefas vencendo nos próximos 7 dias")
        long dueSoonCount) {
}
