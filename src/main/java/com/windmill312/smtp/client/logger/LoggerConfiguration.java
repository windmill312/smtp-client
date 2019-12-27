package com.windmill312.smtp.client.logger;

import com.windmill312.smtp.client.config.ApplicationProperties;
import lombok.Data;

import java.nio.file.Path;
import java.nio.file.Paths;

@Data
class LoggerConfiguration {
    private ApplicationProperties properties;
    private LogLevel level;
    private Path logPath;

    public static final class LoggerConfigurationHolder {
        static final LoggerConfiguration INSTANCE = new LoggerConfiguration();
    }

    private LoggerConfiguration() {
        this.properties = ApplicationProperties.instance();
        this.level = this.properties.getLogLevel();
        this.logPath = Paths.get(properties.getLogPath());
    }

    static LoggerConfiguration instance() {
        return LoggerConfigurationHolder.INSTANCE;
    }
}
