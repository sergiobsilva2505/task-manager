package br.com.forjacode.taskmanager.application.ports.shared;

import br.com.forjacode.taskmanager.application.ports.shared.exception.InvalidPageQueryException;

public record PageQuery(
        int page,
        int size,
        TaskSortField sortBy,
        SortDirection sortDirection) {

    public PageQuery {
        if (page < 0) {
            throw new InvalidPageQueryException("Page index must be >= 0");
        }
        if (size <= 0) {
            throw new InvalidPageQueryException("Page size must be > 0");
        }
        if (sortBy == null) {
            throw new InvalidPageQueryException("SortBy must not be null");
        }
        if (sortDirection == null) {
            throw new InvalidPageQueryException("SortDirection must not be null");
        }
    }
}

