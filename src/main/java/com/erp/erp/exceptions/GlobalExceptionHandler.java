package com.erp.erp.exceptions;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Handle specific exceptions
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(), 
                ex.getMessage(), 
                request.getDescription(false),
                "RESOURCE_NOT_FOUND",
                HttpStatus.NOT_FOUND.value()
        );
        logger.warn("Resource not found: {}", ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDetails> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(), 
                ex.getMessage(), 
                request.getDescription(false),
                "ILLEGAL_ARGUMENT",
                HttpStatus.BAD_REQUEST.value()
        );
        logger.warn("Illegal argument: {}", ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    // Handle validation errors from @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        logger.warn("Validation error: {}", errors);

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", new Date());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("errors", errors);
        body.put("message", "Validation Failed");
        body.put("path", request.getDescription(false));
        body.put("errorCode", "VALIDATION_FAILED");

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // Security-related exception handlers
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDetails> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(), 
                "Access Denied: You do not have permission to access this resource.", 
                request.getDescription(false),
                "ACCESS_DENIED",
                HttpStatus.FORBIDDEN.value()
        );
        logger.warn("Access denied: {} for path {}", ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorDetails> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(), 
                "Invalid username or password", 
                request.getDescription(false),
                "INVALID_CREDENTIALS",
                HttpStatus.UNAUTHORIZED.value()
        );
        logger.warn("Bad credentials: {}", ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorDetails> handleDisabledException(DisabledException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(), 
                "Account is disabled", 
                request.getDescription(false),
                "ACCOUNT_DISABLED",
                HttpStatus.UNAUTHORIZED.value()
        );
        logger.warn("Disabled account: {}", ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorDetails> handleLockedException(LockedException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(), 
                "Account is locked", 
                request.getDescription(false),
                "ACCOUNT_LOCKED",
                HttpStatus.UNAUTHORIZED.value()
        );
        logger.warn("Locked account: {}", ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorDetails> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(), 
                "Authentication failed: " + ex.getMessage(), 
                request.getDescription(false),
                "AUTHENTICATION_FAILED",
                HttpStatus.UNAUTHORIZED.value()
        );
        logger.warn("Authentication failed: {}", ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<ErrorDetails> handleInsufficientAuthenticationException(
            InsufficientAuthenticationException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(), 
                "Full authentication is required to access this resource", 
                request.getDescription(false),
                "INSUFFICIENT_AUTHENTICATION",
                HttpStatus.UNAUTHORIZED.value()
        );
        logger.warn("Insufficient authentication: {}", ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    // JWT-specific exceptions
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorDetails> handleExpiredJwtException(ExpiredJwtException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(), 
                "JWT token has expired", 
                request.getDescription(false),
                "TOKEN_EXPIRED",
                HttpStatus.UNAUTHORIZED.value()
        );
        logger.warn("JWT expired: {}", ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UnsupportedJwtException.class)
    public ResponseEntity<ErrorDetails> handleUnsupportedJwtException(UnsupportedJwtException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(), 
                "JWT token is unsupported", 
                request.getDescription(false),
                "UNSUPPORTED_TOKEN",
                HttpStatus.UNAUTHORIZED.value()
        );
        logger.warn("Unsupported JWT: {}", ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<ErrorDetails> handleMalformedJwtException(MalformedJwtException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(), 
                "Invalid JWT token format", 
                request.getDescription(false),
                "INVALID_TOKEN_FORMAT",
                HttpStatus.UNAUTHORIZED.value()
        );
        logger.warn("Malformed JWT: {}", ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<ErrorDetails> handleSignatureException(SignatureException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(), 
                "Invalid JWT signature", 
                request.getDescription(false),
                "INVALID_TOKEN_SIGNATURE",
                HttpStatus.UNAUTHORIZED.value()
        );
        logger.warn("JWT signature exception: {}", ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorDetails> handleValidationException(ValidationException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(), 
                ex.getMessage(), 
                request.getDescription(false),
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST.value()
        );
        logger.warn("Validation error: {}", ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EmailException.class)
    public ResponseEntity<ErrorDetails> handleEmailException(EmailException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(), 
                ex.getMessage(), 
                request.getDescription(false),
                "EMAIL_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        logger.error("Email error: {}", ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Handle global exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(), 
                "An unexpected error occurred: " + ex.getMessage(), 
                request.getDescription(false),
                "INTERNAL_SERVER_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        logger.error("Global exception handler caught: ", ex);
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Enhanced ErrorDetails class with error code and status
    public static class ErrorDetails {
        private Date timestamp;
        private String message;
        private String details;
        private String errorCode;
        private int status;

        public ErrorDetails(Date timestamp, String message, String details, String errorCode, int status) {
            this.timestamp = timestamp;
            this.message = message;
            this.details = details;
            this.errorCode = errorCode;
            this.status = status;
        }

        public Date getTimestamp() { return timestamp; }
        public String getMessage() { return message; }
        public String getDetails() { return details; }
        public String getErrorCode() { return errorCode; }
        public int getStatus() { return status; }
    }
}
