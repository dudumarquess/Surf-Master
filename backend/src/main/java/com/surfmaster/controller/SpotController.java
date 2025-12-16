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
 * Responsible for listing and detailing registered surf spots.
 */
@RestController
@RequestMapping("/api/spots")
@RequiredArgsConstructor
@Tag(name = "Spots", description = "Surf spot lookup and detailed information.")
public class SpotController {
    private final SpotService spotService;

    /**
     * Returns the list of available spots.
     */
    @Operation(summary = "List available spots", description = "Returns every registered spot with basic information.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List retrieved"),
            @ApiResponse(responseCode = "500", description = "Unexpected error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<?> getListOfSpots() {
        try {
            List<SpotDto> spots = spotService.listAll();
            return ResponseEntity.ok(spots);
        } catch (Exception e) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to list spots right now.", e);
        }
    }

    /**
     * Returns the details of a specific spot.
     */
    @Operation(summary = "Detail a spot", description = "Fetches comprehensive information for a specific spot.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Spot found"),
            @ApiResponse(responseCode = "404", description = "Spot does not exist", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getSpotById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(spotService.getById(id));
        } catch (IllegalArgumentException | NoSuchElementException e) {
            return errorResponse(HttpStatus.NOT_FOUND, "Spot not found for id " + id + ".", e);
        } catch (Exception e) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch spot information.", e);
        }
    }

    private ResponseEntity<ApiErrorResponse> errorResponse(HttpStatus status, String message, Exception e) {
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.of(message, e != null ? e.getMessage() : null));
    }
}
