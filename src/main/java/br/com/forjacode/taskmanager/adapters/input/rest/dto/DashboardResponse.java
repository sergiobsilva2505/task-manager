package br.com.forjacode.taskmanager.adapters.input.rest.dto;

import java.util.Map;

public record DashboardResponse(
        long totalTasks,
        Map<String, Long> countByStatus,
        Map<String, Long> countByPriority,
        long overdueCount,
        long dueSoonCount) {
}
