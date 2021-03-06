package com.windmill312.smtp.client.common.logger;

import com.windmill312.smtp.client.common.config.ApplicationProperties;

import javax.annotation.Nonnull;

import static com.windmill312.smtp.client.common.logger.LogLevel.DEBUG;
import static com.windmill312.smtp.client.common.logger.LogLevel.ERROR;
import static com.windmill312.smtp.client.common.logger.LogLevel.INFO;
import static com.windmill312.smtp.client.common.logger.LogLevel.TRACE;
import static com.windmill312.smtp.client.common.logger.LogLevel.WARN;

public class LoggerImpl implements Logger {

    private final LogLevel logLevel;
    private final LogQueue logQueue;
    private final Class clazz;

    LoggerImpl(Class clazz) {
        this.logLevel = ApplicationProperties.instance().getLogLevel();
        this.logQueue = LogQueue.instance();
        this.clazz = clazz;
    }

    @Override
    public void error(String message, String... values) {
        if (isErrorEnabled()) {
            logQueue.add(prepareLogMessage(ERROR, message));
        }
    }

    @Override
    public void warn(String message, String... values) {
        if (isWarnEnabled()) {
            logQueue.add(prepareLogMessage(WARN, message));
        }
    }

    @Override
    public void info(String message, String... values) {
        if (isInfoEnabled()) {
            logQueue.add(prepareLogMessage(INFO, message));
        }
    }

    @Override
    public void debug(String message, String... values) {
        if (isDebugEnabled()) {
            logQueue.add(prepareLogMessage(DEBUG, message));
        }
    }

    @Override
    public void trace(String message, String... values) {
        if (isTraceEnabled()) {
            logQueue.add(prepareLogMessage(TRACE, message));
        }
    }

    @Override
    public boolean isErrorEnabled() {
        return logLevel.getOrder() >= ERROR.getOrder();
    }

    @Override
    public boolean isWarnEnabled() {
        return logLevel.getOrder() >= WARN.getOrder();
    }

    @Override
    public boolean isInfoEnabled() {
        return logLevel.getOrder() >= INFO.getOrder();
    }

    @Override
    public boolean isDebugEnabled() {
        return logLevel.getOrder() >= DEBUG.getOrder();
    }

    @Override
    public boolean isTraceEnabled() {
        return logLevel.getOrder() >= TRACE.getOrder();
    }

    @Nonnull
    private String prepareLogMessage(
            @Nonnull LogLevel logLevel,
            @Nonnull String message) {
        return "[" + logLevel + "] " + clazz.getCanonicalName() + ": " + message;
    }
}
