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
 * Exposes smart (LLM) daily summaries for surf spots.
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/summaries")
@Tag(name = "Surf Summary", description = "Provides intelligent recaps to plan the surf session.")
public class SurfSummaryController {
    private final SurfSummaryService surfSummaryService;

    /**
     * Returns (or generates) the current day summary for a spot.
     */
    @Operation(summary = "Fetch daily summary", description = "Returns an existing summary or quickly generates one if missing.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary returned"),
            @ApiResponse(responseCode = "404", description = "Spot not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/spot/{spotId}/today")
    public ResponseEntity<?> getTodaySummary(@PathVariable Long spotId) {
        try {
            SurfSummaryDto summary = surfSummaryService.getOrGenerateToday(spotId);
            return ResponseEntity.ok(summary);
        } catch (NoSuchElementException e) {
            return errorResponse(HttpStatus.NOT_FOUND, "We could not find the requested spot.", e);
        } catch (Exception e) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve or generate the summary.", e);
        }
    }

    private ResponseEntity<ApiErrorResponse> errorResponse(HttpStatus status, String message, Exception e) {
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.of(message, e != null ? e.getMessage() : null));
    }
}
