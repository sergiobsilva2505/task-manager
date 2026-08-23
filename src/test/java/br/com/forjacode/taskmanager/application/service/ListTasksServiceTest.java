package br.com.forjacode.taskmanager.application.service;

import br.com.forjacode.taskmanager.application.ports.output.TaskRepositoryPort;
import br.com.forjacode.taskmanager.application.ports.shared.PageQuery;
import br.com.forjacode.taskmanager.application.ports.shared.PagedResult;
import br.com.forjacode.taskmanager.application.ports.shared.SortDirection;
import br.com.forjacode.taskmanager.application.ports.shared.TaskSortField;
import br.com.forjacode.taskmanager.domain.model.Task;
import br.com.forjacode.taskmanager.domain.model.enums.Priority;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListTasksServiceTest {

    @Mock
    private TaskRepositoryPort repositoryPort;

    @InjectMocks
    private ListTasksService listTasksService;

    @Nested
    @DisplayName("Successful retrieval")
    class Success {

        @Test
        @DisplayName("should return paged result when repository returns tasks")
        void shouldReturnPagedResultWhenRepositoryReturnsTasks() {
            PageQuery query = new PageQuery(0, 20, TaskSortField.DEFAULT, SortDirection.DESC);
            UUID ownerId = UUID.randomUUID();
            Task firstTask = Task.create("First task", "First task description", Priority.MEDIUM,
                    LocalDateTime.now().plusDays(1), ownerId);
            Task secondTask = Task.create("Second task", "Second task description", Priority.HIGH,
                    LocalDateTime.now().plusDays(2), ownerId);
            PagedResult<Task> expected = new PagedResult<>(List.of(firstTask, secondTask), 0, 20, 2, 1);
            when(repositoryPort.findAll(query, ownerId)).thenReturn(expected);

            PagedResult<Task> result = listTasksService.execute(query, ownerId);

            assertThat(result).isEqualTo(expected);
            verify(repositoryPort).findAll(query, ownerId);
        }
    }
}