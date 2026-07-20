package br.com.forjacode.taskmanager.application.ports.shared;

import java.util.List;

public record PagedResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {
}
