package com.surfmaster.controller;

import com.surfmaster.dto.ApiErrorResponse;
import com.surfmaster.dto.ChatMessageDto;
import com.surfmaster.dto.ChatSessionDto;
import com.surfmaster.dto.CreateChatMessageRequest;
import com.surfmaster.dto.ResetChatSessionRequest;
import com.surfmaster.service.ChatService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

/**
 * Controller responsible for creating chat sessions and forwarding messages.
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/chat")
@Tag(name = "Chat", description = "Manages chat sessions and message exchange with the Surf Master.")
public class ChatController {
    private final ChatService chatService;

    /**
     * Opens a chat session for a given spot.
     */
    @Operation(summary = "Open a new session", description = "Creates a chat session attached to a spot and optionally to a user.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Session created"),
            @ApiResponse(responseCode = "404", description = "Spot or user not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/sessions/{spotId}")
    public ResponseEntity<?> openSession(@PathVariable Long spotId, @RequestParam(required = false) Long userId) {
        try {
            ChatSessionDto dto = chatService.openSession(spotId, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (NoSuchElementException e) {
            return errorResponse(HttpStatus.NOT_FOUND, "Spot or user not found.", e);
        } catch (Exception e) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create chat session.", e);
        }
    }

    /**
     * Retrieves a session by its identifier.
     */
    @Operation(summary = "Fetch a session", description = "Returns metadata and message history for the session.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session found"),
            @ApiResponse(responseCode = "404", description = "Session not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<?> getSession(@PathVariable Long sessionId) {
        try {
            return ResponseEntity.ok(chatService.getSession(sessionId));
        } catch (NoSuchElementException e) {
            return errorResponse(HttpStatus.NOT_FOUND, "Chat session not found for id " + sessionId + ".", e);
        } catch (Exception e) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch the session.", e);
        }
    }

    /**
     * Registers a new user message.
     */
    @Operation(summary = "Send a message", description = "Accepts user messages and forwards them to the chat service.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Message stored"),
            @ApiResponse(responseCode = "404", description = "Session not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/messages")
    public ResponseEntity<?> sendMessage(@RequestBody CreateChatMessageRequest message) {
        try {
            ChatMessageDto dto = chatService.postUserMessage(message);
            return ResponseEntity.ok(dto);
        } catch (NoSuchElementException e) {
            return errorResponse(HttpStatus.NOT_FOUND, "Chat session not found when storing the message.", e);
        } catch (Exception e) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store the message.", e);
        }
    }

    /**
     * Resets an existing session by changing the spot/user and wiping the history.
     */
    @Operation(summary = "Reset session", description = "Clears the history and updates the spot/user associated with a chat session.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session reset"),
            @ApiResponse(responseCode = "404", description = "Session, spot, or user not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected error", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/sessions/{sessionId}/reset")
    public ResponseEntity<?> resetSession(@PathVariable Long sessionId, @RequestBody ResetChatSessionRequest request) {
        try {
            ChatSessionDto dto = chatService.resetSession(sessionId, request.spotId(), request.userId());
            return ResponseEntity.ok(dto);
        } catch (NoSuchElementException e) {
            return errorResponse(HttpStatus.NOT_FOUND, "Session, spot, or user not found for reset.", e);
        } catch (Exception e) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to reset the chat session.", e);
        }
    }

    private ResponseEntity<ApiErrorResponse> errorResponse(HttpStatus status, String message, Exception e) {
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.of(message, e != null ? e.getMessage() : null));
    }
}
