package com.ecommerce.order.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 400 – validation errors on @RequestBody DTOs
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> body = createBaseBody(HttpStatus.BAD_REQUEST, "Validation Failed");
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                fieldErrors.put(err.getField(), err.getDefaultMessage())
        );
        body.put("fields", fieldErrors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // 400 – invalid/malformed JSON or unreadable request body
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return buildResponse("Malformed JSON request", HttpStatus.BAD_REQUEST);
    }

    // 400 – @RequestParam / @PathVariable constraint violations (e.g., @Min, @NotBlank)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, Object> body = createBaseBody(HttpStatus.BAD_REQUEST, "Constraint Violation");
        Map<String, String> violations = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v ->
                violations.put(v.getPropertyPath().toString(), v.getMessage())
        );
        body.put("fields", violations);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // 400 – wrong type for method argument (e.g., id should be Long but got "abc")
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String param = ex.getName();
        String required = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "required type";
        String msg = "Invalid value for parameter '" + param + "'. Expected " + required + ".";
        return buildResponse(msg, HttpStatus.BAD_REQUEST);
    }

    // 400 – missing required request parameter
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(MissingServletRequestParameterException ex) {
        String msg = "Missing required parameter: " + ex.getParameterName();
        return buildResponse(msg, HttpStatus.BAD_REQUEST);
    }

    // 404 – unmapped routes / static resource not found (Spring 6 / Boot 3.2+)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFound(NoResourceFoundException ex) {
        String msg = "No API endpoint found for path: /" + ex.getResourcePath();
        return buildResponse(msg, HttpStatus.NOT_FOUND);
    }

    // 405 – wrong HTTP method on a valid endpoint
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return buildResponse("Method not allowed for this endpoint", HttpStatus.METHOD_NOT_ALLOWED);
    }

    // 415 – wrong media type
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        return buildResponse("Unsupported media type", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

//    // 401 – authentication failures (e.g., invalid credentials)
//    @ExceptionHandler(AuthenticationException.class)
//    public ResponseEntity<Map<String, Object>> handleAuth(AuthenticationException ex) {
//        return buildResponse("Unauthorized", HttpStatus.UNAUTHORIZED);
//    }
//
//    // 403 – access denied (authenticated but not allowed)
//    @ExceptionHandler(AccessDeniedException.class)
//    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
//        return buildResponse("Forbidden", HttpStatus.FORBIDDEN);
//    }

    // Map ResponseStatusException to its exact status (prevents falling back to 500)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        return buildResponse(message, status);
    }

    // Your domain exceptions (keep your current mapping)
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(EntityNotFoundException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidUserDataException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidUserData(InvalidUserDataException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DatabaseOperationException.class)
    public ResponseEntity<Map<String, Object>> handleDatabaseError(DatabaseOperationException ex) {
        log.error("Database operation error", ex);
        return buildResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 409 – DB unique/foreign key constraint violations (e.g., duplicate email)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        // Avoid leaking DB details; return a generic conflict message
        return buildResponse("Conflict: data integrity violation", HttpStatus.CONFLICT);
    }

    // 500 – everything else
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unhandled exception", ex);
        return buildResponse("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Helpers
    private ResponseEntity<Map<String, Object>> buildResponse(String message, HttpStatus status) {
        Map<String, Object> body = createBaseBody(status, message);
        return new ResponseEntity<>(body, status);
    }

    private Map<String, Object> createBaseBody(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return body;
    }
}
