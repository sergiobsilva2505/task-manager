package br.com.forjacode.taskmanager.application.service;

import br.com.forjacode.taskmanager.application.ports.input.ListTasksUseCase;
import br.com.forjacode.taskmanager.application.ports.output.TaskRepositoryPort;
import br.com.forjacode.taskmanager.application.ports.shared.PageQuery;
import br.com.forjacode.taskmanager.application.ports.shared.PagedResult;
import br.com.forjacode.taskmanager.domain.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class ListTasksService implements ListTasksUseCase {

    private static final Logger log = LoggerFactory.getLogger(ListTasksService.class);

    private final TaskRepositoryPort taskRepositoryPort;

    public ListTasksService(TaskRepositoryPort taskRepositoryPort) {
        this.taskRepositoryPort = taskRepositoryPort;
    }

    @Override
    public PagedResult<Task> execute(PageQuery query, UUID ownerId) {
        log.info("Listing tasks for owner {} with query {}", ownerId, query);
        PagedResult<Task> result = taskRepositoryPort.findAll(query, ownerId);
        log.info("Listed {} tasks for owner {}", result.content().size(), ownerId);
        return result;
    }
}
