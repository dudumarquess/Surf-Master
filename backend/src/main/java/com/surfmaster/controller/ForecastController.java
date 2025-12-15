package com.surfmaster.controller;

import com.surfmaster.dto.ApiErrorResponse;
import com.surfmaster.dto.ForecastDto;
import com.surfmaster.service.ForecastService;
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

/**
 * Endpoints responsáveis por expor previsões de swell, vento e maré por pico.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/forecasts")
@Tag(name = "Forecast", description = "Consulta previsões para cada pico cadastrado.")
public class ForecastController {
    private final ForecastService forecastService;

    /**
     * Lista as previsões mais recentes de um pico.
     */
    @Operation(summary = "Lista previsões por pico", description = "Retorna a previsão mais recente para o pico informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Previsões retornadas"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/spot/{spotId}")
    public ResponseEntity<?> getForecastsBySpot(@PathVariable Long spotId) {
        try {
            List<ForecastDto> forecasts = forecastService.getLatestForSpot(spotId);
            return ResponseEntity.ok(forecasts);
        } catch (Exception e) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível carregar previsões no momento.", e);
        }
    }

    private ResponseEntity<ApiErrorResponse> errorResponse(HttpStatus status, String message, Exception e) {
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.of(message, e != null ? e.getMessage() : null));
    }
}
