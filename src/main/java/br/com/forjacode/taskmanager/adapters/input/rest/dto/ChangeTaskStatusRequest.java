package br.com.forjacode.taskmanager.adapters.input.rest.dto;

import br.com.forjacode.taskmanager.domain.model.enums.Status;
import jakarta.validation.constraints.NotNull;

public record ChangeTaskStatusRequest(@NotNull Status status) {
}
