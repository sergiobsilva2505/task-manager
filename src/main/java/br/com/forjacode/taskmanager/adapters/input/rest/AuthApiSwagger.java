package br.com.forjacode.taskmanager.adapters.input.rest;

import br.com.forjacode.taskmanager.adapters.input.rest.dto.GoogleLoginRequest;
import br.com.forjacode.taskmanager.adapters.input.rest.dto.LoginRequest;
import br.com.forjacode.taskmanager.adapters.input.rest.dto.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

@Tag(name = "Auth", description = "Operações de autenticação")
public interface AuthApiSwagger {

    @Operation(
            summary = "Login com e-mail e senha",
            description = "Autentica um usuário já registrado e emite um token JWT válido por 1 hora."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login bem-sucedido"),
            @ApiResponse(responseCode = "400", description = "Campos obrigatórios ausentes ou e-mail em formato inválido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "E-mail não cadastrado ou senha incorreta (mesma resposta genérica nos dois casos)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    ResponseEntity<LoginResponse> login(LoginRequest request);

    @Operation(
            summary = "Login com Google",
            description = "Valida um ID Token emitido pelo Google e emite um token JWT próprio. " +
                    "Se o e-mail já existir como usuário local, a conta Google é vinculada automaticamente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login bem-sucedido (identidade existente, vinculada, ou usuário novo criado)"),
            @ApiResponse(responseCode = "400", description = "idToken ausente ou em branco",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Token do Google inválido, expirado, ou não emitido para este aplicativo",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    ResponseEntity<LoginResponse> googleLogin(GoogleLoginRequest request);
}