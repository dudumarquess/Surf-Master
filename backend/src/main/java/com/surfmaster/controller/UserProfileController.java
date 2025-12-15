package com.surfmaster.controller;

import com.surfmaster.dto.ApiErrorResponse;
import com.surfmaster.dto.UpsertUserProfileRequest;
import com.surfmaster.dto.UserProfileDto;
import com.surfmaster.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Controlador responsável por expor operações de CRUD para perfis de usuário e suas preferências.
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/profile")
@Tag(name = "User Profile", description = "Gerencia perfis de surfistas e preferências de pranchas")
public class UserProfileController {

    private final UserProfileService userProfileService;

    /**
     * Lista todos os perfis disponíveis.
     */
    @Operation(summary = "Lista todos os perfis de usuário", description = "Retorna todos os perfis cadastrados com suas informações básicas e preferências de prancha.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfis retornados com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<?> listProfiles() {
        try {
            return ResponseEntity.ok(userProfileService.listProfiles());
        } catch (Exception e) {
            return internalError("Não foi possível listar os perfis agora. Tente novamente em instantes.", e);
        }
    }

    /**
     * Obtém um perfil específico via ID.
     */
    @Operation(summary = "Obtém um perfil específico", description = "Busca um perfil existente pelo identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil encontrado"),
            @ApiResponse(responseCode = "404", description = "Perfil não existe", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro inesperado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getProfile(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(userProfileService.getProfile(id));
        } catch (EntityNotFoundException e) {
            return notFound("Perfil não localizado para o id " + id + ".", e);
        } catch (Exception e) {
            return internalError("Erro inesperado ao buscar o perfil.", e);
        }
    }

    /**
     * Cria um novo perfil de usuário.
     */
    @Operation(summary = "Cria um novo perfil", description = "Registra um novo perfil com nível e preferências de prancha.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Perfil criado com sucesso"),
            @ApiResponse(responseCode = "500", description = "Falha ao criar", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<?> createProfile(@RequestBody UpsertUserProfileRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(userProfileService.createProfile(request));
        } catch (Exception e) {
            return internalError("Não foi possível criar o perfil. Reveja os dados e tente novamente.", e);
        }
    }

    /**
     * Atualiza um perfil existente.
     */
    @Operation(summary = "Atualiza um perfil existente", description = "Substitui dados de um perfil mantendo o mesmo identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil atualizado"),
            @ApiResponse(responseCode = "404", description = "Perfil não encontrado para atualização", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro inesperado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable Long id, @RequestBody UpsertUserProfileRequest request) {
        try {
            return ResponseEntity.ok(userProfileService.updateProfile(id, request));
        } catch (IllegalArgumentException e) {
            return notFound("Não foi possível atualizar. Perfil " + id + " inexistente.", e);
        } catch (Exception e) {
            return internalError("Falha ao atualizar o perfil.", e);
        }
    }

    /**
     * Remove um perfil existente.
     */
    @Operation(summary = "Remove um perfil", description = "Exclui o perfil de usuário e suas preferências.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil removido"),
            @ApiResponse(responseCode = "404", description = "Perfil não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro inesperado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProfile(@PathVariable Long id) {
        try {
            userProfileService.deleteProfile(id);
            return ResponseEntity.ok(Map.of(
                    "message", "Perfil removido com sucesso.",
                    "timestamp", OffsetDateTime.now()
            ));
        } catch (IllegalArgumentException e) {
            return notFound("Perfil " + id + " não encontrado.", e);
        } catch (Exception e) {
            return internalError("Não foi possível excluir o perfil.", e);
        }
    }

    private ResponseEntity<ApiErrorResponse> notFound(String message, Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(message, e != null ? e.getMessage() : null));
    }

    private ResponseEntity<ApiErrorResponse> internalError(String message, Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of(message, e != null ? e.getMessage() : null));
    }
}
