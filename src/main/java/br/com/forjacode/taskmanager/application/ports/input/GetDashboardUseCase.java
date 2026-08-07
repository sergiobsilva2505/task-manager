package br.com.forjacode.taskmanager.application.ports.input;

import br.com.forjacode.taskmanager.application.ports.input.result.DashboardResult;

import java.util.UUID;

public interface GetDashboardUseCase {

    DashboardResult execute(UUID ownerId);
}
