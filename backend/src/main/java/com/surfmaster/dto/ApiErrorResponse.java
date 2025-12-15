package com.surfmaster.dto;

import java.time.OffsetDateTime;

/**
 * Representa uma resposta padronizada de erro enviada aos consumidores da API.
 *
 * @param message mensagem amigável descrevendo o problema
 * @param details detalhes técnicos opcionais para apoio ao suporte
 * @param timestamp instante em que o erro foi gerado
 */
public record ApiErrorResponse(
        String message,
        String details,
        OffsetDateTime timestamp
) {
    public static ApiErrorResponse of(String message, String details) {
        return new ApiErrorResponse(message, details, OffsetDateTime.now());
    }
}
