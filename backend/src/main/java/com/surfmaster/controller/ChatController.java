package com.surfmaster.controller;

import com.surfmaster.dto.ApiErrorResponse;
import com.surfmaster.dto.ChatMessageDto;
import com.surfmaster.dto.ChatSessionDto;
import com.surfmaster.dto.CreateChatMessageRequest;
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
 * Controlador responsável por criar sessões de chat e intermediar mensagens.
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/chat")
@Tag(name = "Chat", description = "Gerencia sessões de chat e troca de mensagens com o Mestre do Surf.")
public class ChatController {
    private final ChatService chatService;

    /**
     * Abre uma sessão de chat para um determinado pico.
     */
    @Operation(summary = "Abre uma nova sessão", description = "Cria uma sessão de chat vinculada a um pico e, opcionalmente, a um usuário.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sessão criada"),
            @ApiResponse(responseCode = "404", description = "Pico ou usuário não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro inesperado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/sessions/{spotId}")
    public ResponseEntity<?> openSession(@PathVariable Long spotId, @RequestParam(required = false) Long userId) {
        try {
            ChatSessionDto dto = chatService.openSession(spotId, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (NoSuchElementException e) {
            return errorResponse(HttpStatus.NOT_FOUND, "Não encontramos o pico ou usuário informado.", e);
        } catch (Exception e) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao criar sessão de chat.", e);
        }
    }

    /**
     * Recupera uma sessão pelo identificador.
     */
    @Operation(summary = "Consulta sessão", description = "Retorna metadados e histórico de mensagens da sessão.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sessão encontrada"),
            @ApiResponse(responseCode = "404", description = "Sessão inexistente", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro inesperado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<?> getSession(@PathVariable Long sessionId) {
        try {
            return ResponseEntity.ok(chatService.getSession(sessionId));
        } catch (NoSuchElementException e) {
            return errorResponse(HttpStatus.NOT_FOUND, "Sessão de chat não encontrada para o id " + sessionId + ".", e);
        } catch (Exception e) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao recuperar a sessão.", e);
        }
    }

    /**
     * Registra uma nova mensagem enviada pelo usuário.
     */
    @Operation(summary = "Envia mensagem", description = "Aceita mensagens do usuário e repassa para o serviço de chat.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mensagem registrada"),
            @ApiResponse(responseCode = "404", description = "Sessão não encontrada", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro inesperado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/messages")
    public ResponseEntity<?> sendMessage(@RequestBody CreateChatMessageRequest message) {
        try {
            ChatMessageDto dto = chatService.postUserMessage(message);
            return ResponseEntity.ok(dto);
        } catch (NoSuchElementException e) {
            return errorResponse(HttpStatus.NOT_FOUND, "Sessão de chat não localizada para registrar a mensagem.", e);
        } catch (Exception e) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível registrar a mensagem.", e);
        }
    }

    private ResponseEntity<ApiErrorResponse> errorResponse(HttpStatus status, String message, Exception e) {
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.of(message, e != null ? e.getMessage() : null));
    }
}
