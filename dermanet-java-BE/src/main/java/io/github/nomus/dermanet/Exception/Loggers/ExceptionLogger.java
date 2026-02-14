package io.github.nomus.dermanet.Exception.Loggers;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExceptionLogger {

    public static void logException(Exception e, Class<?> clazz) {
        InternalExceptionLogger.logException(e, clazz);
    }

    public static void logException(String userName, Exception e, Class<?> clazz) {
        UserExceptionLogger.logException(userName, e, clazz);
    }

    public static void logMessage(String message) {
        log.info(message);
    }

    public static void logMessage(String userName, String message) {
        UserExceptionLogger.logMessage(userName, message);
    }

    private ExceptionLogger() {}
}