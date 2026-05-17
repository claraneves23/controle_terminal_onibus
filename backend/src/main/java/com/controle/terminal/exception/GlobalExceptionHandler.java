package com.controle.terminal.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApi(ApiException ex, HttpServletRequest request) {
        String traceId = newTraceId();
        log.warn("[{}] {} {}: {}", traceId, ex.getCode(), request.getRequestURI(), ex.getMessage());
        ApiError body = build(ex.getStatus(), ex.getCode(), ex.getMessage(), request, ex.getDetails(), traceId);
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String traceId = newTraceId();
        List<ApiFieldError> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();
        String message = details.isEmpty()
                ? "Os dados enviados sao invalidos."
                : "Os dados enviados sao invalidos: %d campo(s) com erro.".formatted(details.size());
        log.warn("[{}] VALIDATION_FAILED {} ({} field errors)", traceId, request.getRequestURI(), details.size());
        ApiError body = build(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message, request, details, traceId);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraint(ConstraintViolationException ex, HttpServletRequest request) {
        String traceId = newTraceId();
        List<ApiFieldError> details = ex.getConstraintViolations().stream()
                .map(v -> new ApiFieldError(
                        v.getPropertyPath() == null ? null : v.getPropertyPath().toString(),
                        v.getInvalidValue(),
                        v.getMessage()))
                .toList();
        log.warn("[{}] CONSTRAINT_VIOLATION {}", traceId, request.getRequestURI());
        ApiError body = build(HttpStatus.BAD_REQUEST, "CONSTRAINT_VIOLATION",
                "Parametros invalidos na requisicao.", request, details, traceId);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String traceId = newTraceId();
        log.warn("[{}] MALFORMED_JSON {}: {}", traceId, request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        ApiError body = build(HttpStatus.BAD_REQUEST, "MALFORMED_JSON",
                "Corpo da requisicao mal-formado ou ilegivel.", request, null, traceId);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String traceId = newTraceId();
        String tipo = ex.getRequiredType() == null ? "?" : ex.getRequiredType().getSimpleName();
        String message = "O parametro '%s' deve ser do tipo %s.".formatted(ex.getName(), tipo);
        log.warn("[{}] INVALID_PARAMETER {} ({})", traceId, request.getRequestURI(), message);
        ApiError body = build(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", message, request, null, traceId);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        String traceId = newTraceId();
        String causaTexto = ex.getMostSpecificCause() == null ? "" : ex.getMostSpecificCause().getMessage();
        String message;
        String code;
        if (causaTexto != null && causaTexto.toLowerCase().contains("unique")) {
            code = "DUPLICATE_RESOURCE";
            message = "Ja existe um registro com os mesmos dados unicos.";
        } else if (causaTexto != null && causaTexto.toLowerCase().contains("foreign key")) {
            code = "FOREIGN_KEY_VIOLATION";
            message = "Operacao nao permitida: existe outro registro vinculado a este.";
        } else {
            code = "DATA_INTEGRITY_VIOLATION";
            message = "A operacao viola uma regra de integridade do banco.";
        }
        log.warn("[{}] {} {}: {}", traceId, code, request.getRequestURI(), causaTexto);
        ApiError body = build(HttpStatus.CONFLICT, code, message, request, null, traceId);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        String traceId = newTraceId();
        log.warn("[{}] INVALID_CREDENTIALS {}", traceId, request.getRequestURI());
        ApiError body = build(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                "Email ou senha incorretos.", request, null, traceId);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        String traceId = newTraceId();
        log.warn("[{}] FORBIDDEN {}", traceId, request.getRequestURI());
        ApiError body = build(HttpStatus.FORBIDDEN, "FORBIDDEN",
                "Voce nao tem permissao para executar esta acao.", request, null, traceId);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuth(AuthenticationException ex, HttpServletRequest request) {
        String traceId = newTraceId();
        log.warn("[{}] UNAUTHENTICATED {}: {}", traceId, request.getRequestURI(), ex.getMessage());
        ApiError body = build(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED",
                "Voce precisa estar autenticado para acessar este recurso.", request, null, traceId);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiError> handleNoHandler(NoHandlerFoundException ex, HttpServletRequest request) {
        String traceId = newTraceId();
        log.warn("[{}] ENDPOINT_NOT_FOUND {} {}", traceId, ex.getHttpMethod(), ex.getRequestURL());
        ApiError body = build(HttpStatus.NOT_FOUND, "ENDPOINT_NOT_FOUND",
                "Endpoint nao encontrado: %s %s".formatted(ex.getHttpMethod(), ex.getRequestURL()),
                request, null, traceId);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethod(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String traceId = newTraceId();
        log.warn("[{}] METHOD_NOT_ALLOWED {} {}", traceId, ex.getMethod(), request.getRequestURI());
        ApiError body = build(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED",
                "Metodo HTTP '%s' nao e suportado neste endpoint.".formatted(ex.getMethod()),
                request, null, traceId);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleFallback(Exception ex, HttpServletRequest request) {
        String traceId = newTraceId();
        log.error("[{}] INTERNAL_ERROR {}", traceId, request.getRequestURI(), ex);
        ApiError body = build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Erro interno do servidor. Use o traceId para suporte: " + traceId,
                request, null, traceId);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private ApiFieldError toFieldError(FieldError f) {
        return new ApiFieldError(f.getField(), f.getRejectedValue(), f.getDefaultMessage());
    }

    private ApiError build(HttpStatus status, String code, String message,
                           HttpServletRequest request, List<ApiFieldError> details, String traceId) {
        return new ApiError(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                request.getRequestURI(),
                details == null || details.isEmpty() ? null : details,
                traceId
        );
    }

    private String newTraceId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
