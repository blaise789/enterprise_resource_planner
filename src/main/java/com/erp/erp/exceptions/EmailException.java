package com.erp.erp.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when there is an error related to email operations.
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class EmailException extends RuntimeException {
    
    public EmailException(String message) {
        super(message);
    }
    
    public EmailException(String message, Throwable cause) {
        super(message, cause);
    }
}