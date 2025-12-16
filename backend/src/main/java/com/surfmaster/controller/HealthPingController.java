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
 * Simple availability endpoint.
 */
@RestController
@Tag(name = "Health", description = "Checks whether the backend is up.")
public class HealthPingController {
    @Operation(summary = "Health ping", description = "Returns a simple payload confirming the service is alive.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service responding"),
            @ApiResponse(responseCode = "500", description = "Unexpected failure", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
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
                    .body(ApiErrorResponse.of("Health service unavailable.", e.getMessage()));
        }
    }
}
