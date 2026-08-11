package br.com.forjacode.taskmanager.adapters.input.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record PagedResponse<T>(
        @Schema(description = "Itens da página atual")
        List<T> content,
        @Schema(description = "Número da página atual (a partir de 0)", example = "0")
        int page,
        @Schema(description = "Quantidade de itens por página", example = "20")
        int size,
        @Schema(description = "Total de elementos disponíveis")
        long totalElements,
        @Schema(description = "Total de páginas disponíveis")
        int totalPages) {
}
