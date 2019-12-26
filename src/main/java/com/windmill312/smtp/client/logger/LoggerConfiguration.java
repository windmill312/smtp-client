package com.windmill312.smtp.client.logger;

import lombok.Data;

import static com.windmill312.smtp.client.logger.LogLevel.INFO;

@Data
class LoggerConfiguration {
    private LogLevel level = INFO;

    public static final class LoggerConfigurationHolder {
        static final LoggerConfiguration INSTANCE = new LoggerConfiguration();
    }

    private LoggerConfiguration() {}

    static LoggerConfiguration instance() {
        return LoggerConfigurationHolder.INSTANCE;
    }
}
