package com.logiflow.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Standard error response returned by the API")
public record ErrorResponse(
        @Schema(description = "HTTP status code", example = "400")
        int status,

        @Schema(description = "Error message describing what went wrong", example = "Validation Failed")
        String message,

        @Schema(description = "Field-level validation errors (field name -> error message)", example = "{\"name\": \"Product name must not be blank\"}")
        Map<String, String> errors,

        @Schema(description = "Timestamp when the error occurred", example = "2025-12-27T10:30:00")
        LocalDateTime timestamp
) {
}
