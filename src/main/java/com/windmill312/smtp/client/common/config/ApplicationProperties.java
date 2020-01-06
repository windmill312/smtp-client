package com.windmill312.smtp.client.common.config;

import com.windmill312.smtp.client.common.logger.LogLevel;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ApplicationProperties {

    @Getter
    private final String mailDir;

    @Getter
    private final String logPath;

    @Getter
    private final LogLevel logLevel;

    @Getter
    private final int socketTimeoutMs;

    @Getter
    private final int bufferSize;

    @Getter
    private final int batchSize;

    public static final class ApplicationPropertiesHolder {
        static final ApplicationProperties instance = new ApplicationProperties();
    }

    private ApplicationProperties() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("application.properties");
        Properties appProps = new Properties();

        try {
            appProps.load(inputStream);
        } catch (IOException e) {
            System.out.println("Got error while reading application.properties: " + e.getLocalizedMessage());
        }

        this.mailDir = appProps.getProperty("mail.dir");
        this.logPath = appProps.getProperty("logger.file-path");
        this.logLevel = LogLevel.valueOf(appProps.getProperty("logger.level"));
        this.socketTimeoutMs = Integer.valueOf(appProps.getProperty("socket.timeout.ms"));
        this.bufferSize = Integer.valueOf(appProps.getProperty("buffer.size"));
        this.batchSize = Integer.valueOf(appProps.getProperty("batch.size"));
    }

    public static ApplicationProperties instance() {
        return ApplicationPropertiesHolder.instance;
    }
}
