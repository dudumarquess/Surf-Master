// src/main/java/com/surfmaster/controller/HealthPingController.java
package com.surfmaster.controller;

import com.surfmaster.dto.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Endpoint simples de verificação de disponibilidade.
 */
@RestController
@Tag(name = "Health", description = "Verifica se o backend está operacional.")
public class HealthPingController {
    @Operation(summary = "Ping de saúde", description = "Retorna um payload básico confirmando que o serviço está ativo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Serviço respondendo"),
            @ApiResponse(responseCode = "500", description = "Falha inesperada", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        try {
            return ResponseEntity.ok(Map.of(
                    "status", "pong",
                    "timestamp", OffsetDateTime.now()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiErrorResponse.of("Serviço de saúde indisponível.", e.getMessage()));
        }
    }
}
