package com.surfmaster.controller;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.surfmaster.dto.ApiErrorResponse;
import com.surfmaster.dto.ForecastDto;
import com.surfmaster.dto.ForecastSyncResponse;
import com.surfmaster.service.ForecastService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

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
    public ResponseEntity<?> getForecastsBySpot(
            @PathVariable Long spotId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to
    ) {
        try {
            List<ForecastDto> forecasts = forecastService.getForecastsForSpot(spotId, from, to);
            return ResponseEntity.ok(forecasts);
        } catch (IllegalArgumentException e) {
            return errorResponse(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível carregar previsões no momento.", e);
        }
    }

    @Operation(summary = "Sincroniza previsões via Stormglass", description = "Chama manualmente o provedor configurado para atualizar previsões. Útil quando há limite diário de requisições.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sincronização realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro inesperado ao sincronizar", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/sync")
    public ResponseEntity<?> syncForecasts(
            @RequestParam(required = false) Long spotId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(defaultValue = "false") boolean force
    ) {
        try {
            ForecastSyncResponse response = forecastService.syncWithProvider(spotId, from, to, force);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível sincronizar previsões agora.", e);
        }
    }

    private ResponseEntity<ApiErrorResponse> errorResponse(HttpStatus status, String message, Exception e) {
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.of(message, e != null ? e.getMessage() : null));
    }
}
