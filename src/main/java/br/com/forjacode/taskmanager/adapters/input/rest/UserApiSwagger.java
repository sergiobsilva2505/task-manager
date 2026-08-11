package br.com.forjacode.taskmanager.adapters.input.rest;

import br.com.forjacode.taskmanager.adapters.input.rest.dto.CreateUserRequest;
import br.com.forjacode.taskmanager.adapters.input.rest.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

@Tag(name = "Users", description = "Operações de gerenciamento de usuários")
public interface UserApiSwagger {

    @Operation(
            summary = "Registra um novo usuário",
            description = "Cria o usuário e uma credencial local (senha), como uma transação atômica. " +
                    "A senha nunca é retornada em nenhuma resposta da API."
    )
    @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso")
    @ApiResponse(responseCode = "400",
            description = "Campos ausentes/inválidos: nome fora do formato/tamanho esperado, e-mail em formato " +
                    "inválido, ou senha sem os requisitos mínimos de força",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "E-mail já está em uso por outro usuário",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    ResponseEntity<UserResponse> createUser(CreateUserRequest request);
}