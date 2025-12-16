package com.surfmaster.controller;

import com.surfmaster.dto.ApiErrorResponse;
import com.surfmaster.dto.UpsertUserProfileRequest;
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
 * Controller exposing CRUD operations for user profiles and their preferences.
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/profile")
@Tag(name = "User Profile", description = "Manages surfer profiles and board preferences.")
public class UserProfileController {

    private final UserProfileService userProfileService;

    /**
     * Lists all profiles.
     */
    @Operation(summary = "List all user profiles", description = "Returns every stored profile with basic information and board preferences.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profiles returned successfully"),
            @ApiResponse(responseCode = "500", description = "Unexpected error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<?> listProfiles() {
        try {
            return ResponseEntity.ok(userProfileService.listProfiles());
        } catch (Exception e) {
            return internalError("Could not list profiles right now. Please try again shortly.", e);
        }
    }

    /**
     * Retrieves a specific profile by id.
     */
    @Operation(summary = "Get a specific profile", description = "Fetches an existing profile by its identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile found"),
            @ApiResponse(responseCode = "404", description = "Profile does not exist", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getProfile(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(userProfileService.getProfile(id));
        } catch (EntityNotFoundException e) {
            return notFound("Profile not found for id " + id + ".", e);
        } catch (Exception e) {
            return internalError("Unexpected error while fetching the profile.", e);
        }
    }

    /**
     * Creates a new user profile.
     */
    @Operation(summary = "Create a new profile", description = "Registers a new profile with level and board preferences.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Profile created successfully"),
            @ApiResponse(responseCode = "500", description = "Failed to create", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<?> createProfile(@RequestBody UpsertUserProfileRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(userProfileService.createProfile(request));
        } catch (Exception e) {
            return internalError("Could not create the profile. Review the data and try again.", e);
        }
    }

    /**
     * Updates an existing profile.
     */
    @Operation(summary = "Update an existing profile", description = "Replaces profile data while keeping the identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated"),
            @ApiResponse(responseCode = "404", description = "Profile not found for update", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable Long id, @RequestBody UpsertUserProfileRequest request) {
        try {
            return ResponseEntity.ok(userProfileService.updateProfile(id, request));
        } catch (IllegalArgumentException e) {
            return notFound("Could not update. Profile " + id + " does not exist.", e);
        } catch (Exception e) {
            return internalError("Failed to update the profile.", e);
        }
    }

    /**
     * Removes an existing profile.
     */
    @Operation(summary = "Delete a profile", description = "Deletes the user profile and its preferences.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile removed"),
            @ApiResponse(responseCode = "404", description = "Profile not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProfile(@PathVariable Long id) {
        try {
            userProfileService.deleteProfile(id);
            return ResponseEntity.ok(Map.of(
                    "message", "Profile removed successfully.",
                    "timestamp", OffsetDateTime.now()
            ));
        } catch (IllegalArgumentException e) {
            return notFound("Profile " + id + " not found.", e);
        } catch (Exception e) {
            return internalError("Could not delete the profile.", e);
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
