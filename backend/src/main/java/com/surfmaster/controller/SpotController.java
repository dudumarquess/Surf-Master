package com.surfmaster.controller;

import com.surfmaster.dto.ApiErrorResponse;
import com.surfmaster.dto.SpotDto;
import com.surfmaster.service.SpotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Responsável por listar e detalhar picos cadastrados na plataforma.
 */
@RestController
@RequestMapping("/api/spots")
@RequiredArgsConstructor
@Tag(name = "Spots", description = "Consulta de picos e informações detalhadas.")
public class SpotController {
    private final SpotService spotService;

    /**
     * Retorna a lista de picos disponíveis.
     */
    @Operation(summary = "Lista picos disponíveis", description = "Retorna todos os picos cadastrados com informações básicas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista recuperada"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<?> getListOfSpots() {
        try {
            List<SpotDto> spots = spotService.listAll();
            return ResponseEntity.ok(spots);
        } catch (Exception e) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível listar os picos no momento.", e);
        }
    }

    /**
     * Retorna os detalhes de um pico específico.
     */
    @Operation(summary = "Detalha um pico", description = "Busca informações completas para um pico específico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pico encontrado"),
            @ApiResponse(responseCode = "404", description = "Pico não existe", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro inesperado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getSpotById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(spotService.getById(id));
        } catch (IllegalArgumentException | NoSuchElementException e) {
            return errorResponse(HttpStatus.NOT_FOUND, "Pico não encontrado para o id " + id + ".", e);
        } catch (Exception e) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao buscar informações do pico.", e);
        }
    }

    private ResponseEntity<ApiErrorResponse> errorResponse(HttpStatus status, String message, Exception e) {
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.of(message, e != null ? e.getMessage() : null));
    }
}
