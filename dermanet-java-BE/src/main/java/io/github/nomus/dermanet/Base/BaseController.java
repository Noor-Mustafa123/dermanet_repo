package io.github.nomus.dermanet.Base;

import org.springframework.http.ResponseEntity;

import io.github.nomus.dermanet.Exception.Exceptions.DermaException;
import io.github.nomus.dermanet.Exception.Loggers.ExceptionLogger;
import io.github.nomus.dermanet.Exception.Loggers.InternalExceptionLogger;

public class BaseController {

    protected ResponseEntity handleException( Exception exception ) {

        if ( exception instanceof DermaException dermaException ) {
            //TODO: add user functionaltiy into the logs being logged
            //TODO: add common messeges in an enum like simuspace has used enums
            ExceptionLogger.logException( user, susException, getClass() );
            return ResponseEntity.status( ( ( DermaException ) exception ).getStatusCode() ).body( exception.getMessage() );
        }
        if ( exception instanceof SusDataBaseException susDataBaseException ) {
            ExceptionLogger.logException( userName, susDataBaseException, getClass() );
            return
        } else {
            InternalExceptionLogger.logException( exception, getClass() );
            return

        }
    }
}
