package io.github.nomus.dermanet.Base;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.github.nomus.dermanet.exception.ErrorResponse;
import io.github.nomus.dermanet.Exception.Exceptions.DermaException;
import io.github.nomus.dermanet.Exception.Loggers.ExceptionLogger;
import io.github.nomus.dermanet.Exception.Loggers.InternalExceptionLogger;

public class BaseController {

    protected ResponseEntity<ErrorResponse> handleException(Exception exception) {
        if (exception instanceof DermaException dermaException) {
            ExceptionLogger.logException(exception, getClass());
            return ResponseEntity.status(dermaException.getStatusCode())
                    .body(new ErrorResponse(exception.getMessage(), "DermaException", dermaException.getStatusCode()));
        } else {
            InternalExceptionLogger.logException(exception, getClass());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An unexpected error occurred", exception.getMessage(), 
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}
