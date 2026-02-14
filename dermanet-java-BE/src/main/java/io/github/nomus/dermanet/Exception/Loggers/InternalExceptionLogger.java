package io.github.nomus.dermanet.Exception.Loggers;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InternalExceptionLogger {

    public static void logException(Exception e, Class<?> clazz) {
        log.error("Exception in {}: {}", clazz.getSimpleName(), e.getMessage(), e);
    }

    private InternalExceptionLogger() {}
}
