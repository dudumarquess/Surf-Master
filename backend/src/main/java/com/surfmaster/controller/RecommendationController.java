package com.surfmaster.controller;

import com.surfmaster.dto.ApiErrorResponse;
import com.surfmaster.dto.RecommendationRequest;
import com.surfmaster.dto.RecommendationResponse;
import com.surfmaster.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints responsáveis por gerar recomendações personalizadas de picos.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendations")
@Tag(name = "Recommendations", description = "Sugere melhores picos/horários com base nas preferências do usuário.")
public class RecommendationController {

    private final RecommendationService recommendationService;

    /**
     * Gera recomendações com base nos filtros enviados pelo consumidor.
     */
    @Operation(summary = "Gera recomendações", description = "Avalia previsões e ranqueia os melhores picos para o período informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recomendações calculadas com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro inesperado ao gerar recomendações", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<?> recommend(@Valid @RequestBody RecommendationRequest request) {
        try {
            RecommendationResponse response = recommendationService.recommend(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível gerar recomendações neste momento.", e);
        }
    }

    private ResponseEntity<ApiErrorResponse> errorResponse(HttpStatus status, String message, Exception e) {
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.of(message, e != null ? e.getMessage() : null));
    }
}
