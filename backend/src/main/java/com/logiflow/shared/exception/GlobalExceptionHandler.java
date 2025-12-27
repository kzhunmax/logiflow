package com.logiflow.shared.exception;

import com.logiflow.shared.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation Failed", errors);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleInsufficientStockException(InsufficientStockException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    @ExceptionHandler(InventoryNotFoundException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleInventoryNotFoundException(InventoryNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleProductNotFoundException(ProductNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<@NonNull ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", null);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message, Map<String, String> details) {
        ErrorResponse response = new ErrorResponse(
                status.value(),
                message,
                details,
                LocalDateTime.now()
        );
        return ResponseEntity.status(status).body(response);
    }
}
