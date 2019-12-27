package com.windmill312.smtp.client.logger;

import com.windmill312.smtp.client.queue.LogQueue;

import javax.annotation.Nonnull;

import static com.windmill312.smtp.client.logger.LogLevel.*;

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
    public boolean isErrorEnabled() {
        return loggerConfiguration.getLevel().getOrder() >= ERROR.getOrder();
    }

    @Override
    public boolean isWarnEnabled() {
        return loggerConfiguration.getLevel().getOrder() >= WARN.getOrder();
    }

    @Override
    public boolean isInfoEnabled() {
        return loggerConfiguration.getLevel().getOrder() >= INFO.getOrder();
    }

    @Override
    public boolean isDebugEnabled() {
        return loggerConfiguration.getLevel().getOrder() >= DEBUG.getOrder();
    }

    @Nonnull
    private String prepareLogMessage(
            @Nonnull LogLevel logLevel,
            @Nonnull String message) {
        return clazz.getSimpleName() + ": " + logLevel + " " + message;
    }
}
