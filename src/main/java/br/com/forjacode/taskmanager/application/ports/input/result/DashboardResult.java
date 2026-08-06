package br.com.forjacode.taskmanager.application.ports.input.result;

import br.com.forjacode.taskmanager.domain.model.enums.Priority;
import br.com.forjacode.taskmanager.domain.model.enums.Status;

import java.util.Map;

public record DashboardResult(
        long totalTasks,
        Map<Status, Long> countByStatus,
        Map<Priority, Long> countByPriority,
        long overdueCount,
        long dueSoonCount) {
}
