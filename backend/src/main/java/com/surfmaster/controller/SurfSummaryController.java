package com.surfmaster.controller;

import com.surfmaster.dto.ApiErrorResponse;
import com.surfmaster.dto.SurfSummaryDto;
import com.surfmaster.service.SurfSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

/**
 * Expõe os resumos inteligentes (LLM) para os picos.
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/summaries")
@Tag(name = "Surf Summary", description = "Fornece resumos inteligentes para planejar o surf.")
public class SurfSummaryController {
    private final SurfSummaryService surfSummaryService;

    /**
     * Retorna (ou gera) o resumo do dia para um pico.
     */
    @Operation(summary = "Obtém resumo do dia", description = "Busca um resumo pronto ou gera rapidamente caso não exista.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumo retornado"),
            @ApiResponse(responseCode = "404", description = "Pico não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro inesperado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/spot/{spotId}/today")
    public ResponseEntity<?> getTodaySummary(@PathVariable Long spotId) {
        try {
            SurfSummaryDto summary = surfSummaryService.getOrGenerateToday(spotId);
            return ResponseEntity.ok(summary);
        } catch (NoSuchElementException e) {
            return errorResponse(HttpStatus.NOT_FOUND, "Não conseguimos localizar o pico informado.", e);
        } catch (Exception e) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao recuperar ou gerar o resumo.", e);
        }
    }

    private ResponseEntity<ApiErrorResponse> errorResponse(HttpStatus status, String message, Exception e) {
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.of(message, e != null ? e.getMessage() : null));
    }
}
