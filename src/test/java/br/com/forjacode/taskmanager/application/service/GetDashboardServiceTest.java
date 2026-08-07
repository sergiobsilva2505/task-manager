package br.com.forjacode.taskmanager.application.service;

import br.com.forjacode.taskmanager.application.ports.input.result.DashboardResult;
import br.com.forjacode.taskmanager.application.ports.output.TaskRepositoryPort;
import br.com.forjacode.taskmanager.domain.model.Task;
import br.com.forjacode.taskmanager.domain.model.enums.Priority;
import br.com.forjacode.taskmanager.domain.model.enums.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetDashboardServiceTest {

    @Mock
    private TaskRepositoryPort repositoryPort;

    @InjectMocks
    private GetDashboardService getDashboardService;

    private UUID ownerId;

    @Nested
    @DisplayName("Successful dashboard retrieval")
    class Success {

        @Test
        @DisplayName("should compute all metrics from a mixed set of tasks in a single pass")
        void shouldComputeAllMetricsFromAMixedSetOfTasksInASinglePass() {
            ownerId = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();

            Task overdueTask = Task.reconstruct(UUID.randomUUID(), "Overdue task", "description", Status.TODO,
                    Priority.HIGH, now.minusDays(1), ownerId, now.minusDays(5).toInstant(ZoneOffset.UTC),
                    now.minusDays(5).toInstant(ZoneOffset.UTC));

            Task dueSoonTask = Task.reconstruct(UUID.randomUUID(), "Due soon task", "description", Status.IN_PROGRESS,
                    Priority.MEDIUM, now.plusDays(3), ownerId, now.minusDays(1).toInstant(ZoneOffset.UTC),
                    now.minusDays(1).toInstant(ZoneOffset.UTC));

            Task terminalPastDueTask = Task.reconstruct(UUID.randomUUID(), "Done task", "description", Status.DONE,
                    Priority.LOW, now.minusDays(10), ownerId, now.minusDays(15).toInstant(ZoneOffset.UTC),
                    now.minusDays(15).toInstant(ZoneOffset.UTC));

            Task farFutureTask = Task.reconstruct(UUID.randomUUID(), "Far future task", "description", Status.TODO,
                    Priority.HIGH, now.plusDays(20), ownerId, now.minusDays(1).toInstant(ZoneOffset.UTC),
                    now.minusDays(1).toInstant(ZoneOffset.UTC));

            List<Task> tasks = List.of(overdueTask, dueSoonTask, terminalPastDueTask, farFutureTask);
            when(repositoryPort.findAllByOwnerId(ownerId)).thenReturn(tasks);

            DashboardResult result = getDashboardService.execute(ownerId);

            assertThat(result.totalTasks()).isEqualTo(4);
            assertThat(result.countByStatus()).containsExactlyInAnyOrderEntriesOf(Map.of(
                    Status.TODO, 2L,
                    Status.IN_PROGRESS, 1L,
                    Status.DONE, 1L));
            assertThat(result.countByPriority()).containsExactlyInAnyOrderEntriesOf(Map.of(
                    Priority.HIGH, 2L,
                    Priority.MEDIUM, 1L,
                    Priority.LOW, 1L));
            assertThat(result.overdueCount())
                    .as("only the non-terminal task past its due date should count as overdue; the DONE task must not")
                    .isEqualTo(1);
            assertThat(result.dueSoonCount()).isEqualTo(1);
            verify(repositoryPort).findAllByOwnerId(ownerId);
        }

        @Test
        @DisplayName("should NOT count a DONE task with a past due date as overdue, since terminal statuses are excluded")
        void shouldNotCountADoneTaskWithAPastDueDateAsOverdueSinceTerminalStatusesAreExcluded() {
            ownerId = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();

            Task terminalPastDueTask = Task.reconstruct(UUID.randomUUID(), "Done task", "description", Status.DONE,
                    Priority.LOW, now.minusDays(10), ownerId, now.minusDays(15).toInstant(ZoneOffset.UTC),
                    now.minusDays(15).toInstant(ZoneOffset.UTC));

            when(repositoryPort.findAllByOwnerId(ownerId)).thenReturn(List.of(terminalPastDueTask));

            DashboardResult result = getDashboardService.execute(ownerId);

            assertThat(result.overdueCount())
                    .as("DONE is a terminal status, so a past due date must not count as overdue")
                    .isZero();
            assertThat(result.dueSoonCount()).isZero();
        }
    }

    @Nested
    @DisplayName("Dashboard retrieval with no tasks")
    class WithError {

        @Test
        @DisplayName("should return zeroed metrics when owner has no tasks")
        void shouldReturnZeroedMetricsWhenOwnerHasNoTasks() {
            ownerId = UUID.randomUUID();
            when(repositoryPort.findAllByOwnerId(ownerId)).thenReturn(List.of());

            DashboardResult result = getDashboardService.execute(ownerId);

            assertThat(result.totalTasks()).isZero();
            assertThat(result.countByStatus()).isEmpty();
            assertThat(result.countByPriority()).isEmpty();
            assertThat(result.overdueCount()).isZero();
            assertThat(result.dueSoonCount()).isZero();
            verify(repositoryPort).findAllByOwnerId(ownerId);
        }
    }
}