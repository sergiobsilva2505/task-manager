package br.com.forjacode.taskmanager.application.service;

import br.com.forjacode.taskmanager.application.ports.input.GetDashboardUseCase;
import br.com.forjacode.taskmanager.application.ports.input.result.DashboardResult;
import br.com.forjacode.taskmanager.application.ports.output.TaskRepositoryPort;
import br.com.forjacode.taskmanager.domain.model.Task;
import br.com.forjacode.taskmanager.domain.model.enums.Priority;
import br.com.forjacode.taskmanager.domain.model.enums.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GetDashboardService implements GetDashboardUseCase {

    private static final int DUE_SOON_DAYS = 7;

    private final TaskRepositoryPort taskRepositoryPort;

    public GetDashboardService(TaskRepositoryPort taskRepositoryPort) {
        this.taskRepositoryPort = taskRepositoryPort;
    }

    @Override
    public DashboardResult execute(UUID ownerId) {
        List<Task> tasks = taskRepositoryPort.findAllByOwnerId(ownerId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueSoonThreshold = now.plusDays(DUE_SOON_DAYS);

        Map<Status, Long> countByStatus = tasks.stream()
                .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));

        Map<Priority, Long> countByPriority = tasks.stream()
                .collect(Collectors.groupingBy(Task::getPriority, Collectors.counting()));

        long overdueCount = tasks.stream()
                .filter(task -> !task.getStatus().isTerminal())
                .filter(task -> task.getDueDate().isBefore(now))
                .count();

        long dueSoonCount = tasks.stream()
                .filter(task -> !task.getStatus().isTerminal())
                .filter(task -> !task.getDueDate().isBefore(now))
                .filter(task -> task.getDueDate().isBefore(dueSoonThreshold))
                .count();

        return new DashboardResult(tasks.size(), countByStatus, countByPriority, overdueCount, dueSoonCount);
    }
}