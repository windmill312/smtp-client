package com.windmill312.smtp.client.common.logger;

import com.windmill312.smtp.client.common.config.ApplicationProperties;
import lombok.Data;

@Data
class LoggerConfiguration {
    private ApplicationProperties properties;

    public static final class LoggerConfigurationHolder {
        static final LoggerConfiguration INSTANCE = new LoggerConfiguration();
    }

    private LoggerConfiguration() {
        this.properties = ApplicationProperties.instance();
    }

    static LoggerConfiguration instance() {
        return LoggerConfigurationHolder.INSTANCE;
    }
}