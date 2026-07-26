package br.com.forjacode.taskmanager.application.service;

import br.com.forjacode.taskmanager.application.ports.output.TaskRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeleteTaskServiceTest {

    @Mock
    private TaskRepositoryPort taskRepository;

    @InjectMocks
    private DeleteTaskService deleteTaskService;

    @Test
    @DisplayName("should delegate deletion to repository by id and owner id")
    void shouldDelegateDeletionToRepositoryByIdAndOwnerId() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        deleteTaskService.execute(taskId, userId);

        verify(taskRepository).deleteByIdAndOwnerId(taskId, userId);
    }
}