package com.windmill312.smtp.client.common.logger;

import javax.annotation.Nonnull;

import static com.windmill312.smtp.client.common.logger.LogLevel.DEBUG;
import static com.windmill312.smtp.client.common.logger.LogLevel.ERROR;
import static com.windmill312.smtp.client.common.logger.LogLevel.INFO;
import static com.windmill312.smtp.client.common.logger.LogLevel.TRACE;
import static com.windmill312.smtp.client.common.logger.LogLevel.WARN;

public class LoggerImpl implements Logger {

    private final LoggerConfiguration loggerConfiguration;
    private final LogQueue logQueue;
    private final Class clazz;

    LoggerImpl(Class clazz) {
        this.loggerConfiguration = LoggerConfiguration.instance();
        this.logQueue = LogQueue.instance();
        this.clazz = clazz;
    }

    @Override
    public void error(String message, String... values) {
        if (isErrorEnabled()) {
            logQueue.enqueue(prepareLogMessage(ERROR, message));
        }
    }

    @Override
    public void warn(String message, String... values) {
        if (isWarnEnabled()) {
            logQueue.enqueue(prepareLogMessage(WARN, message));
        }
    }

    @Override
    public void info(String message, String... values) {
        if (isInfoEnabled()) {
            logQueue.enqueue(prepareLogMessage(INFO, message));
        }
    }

    @Override
    public void debug(String message, String... values) {
        if (isDebugEnabled()) {
            logQueue.enqueue(prepareLogMessage(DEBUG, message));
        }
    }

    @Override
    public void trace(String message, String... values) {
        if (isTraceEnabled()) {
            logQueue.enqueue(prepareLogMessage(TRACE, message));
        }
    }

    @Override
    public boolean isErrorEnabled() {
        return loggerConfiguration.getProperties().getLogLevel().getOrder() >= ERROR.getOrder();
    }

    @Override
    public boolean isWarnEnabled() {
        return loggerConfiguration.getProperties().getLogLevel().getOrder() >= WARN.getOrder();
    }

    @Override
    public boolean isInfoEnabled() {
        return loggerConfiguration.getProperties().getLogLevel().getOrder() >= INFO.getOrder();
    }

    @Override
    public boolean isDebugEnabled() {
        return loggerConfiguration.getProperties().getLogLevel().getOrder() >= DEBUG.getOrder();
    }

    @Override
    public boolean isTraceEnabled() {
        return loggerConfiguration.getProperties().getLogLevel().getOrder() >= TRACE.getOrder();
    }

    @Nonnull
    private String prepareLogMessage(
            @Nonnull LogLevel logLevel,
            @Nonnull String message) {
        return "[" + logLevel + "] " + clazz.getCanonicalName() + ": " + message;
    }
}
