package io.github.nomus.dermanet.Exception.Loggers;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserExceptionLogger {

    public static void logException(String userName, Exception e, Class<?> clazz) {
        log.error("User: {} - Exception in {}: {}", userName, clazz.getSimpleName(), e.getMessage(), e);
    }

    public static void logMessage(String userName, String message) {
        log.info("User: {} - {}", userName, message);
    }

    private UserExceptionLogger() {}
}
