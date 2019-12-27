package com.windmill312.smtp.client.config;

import com.windmill312.smtp.client.logger.LogLevel;
import lombok.Getter;

import java.io.FileInputStream;
import java.io.IOException;
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

    public static final class ApplicationPropertiesHolder {
        static final ApplicationProperties instance = new ApplicationProperties();
    }

    private ApplicationProperties() {
        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String appConfigPath = rootPath + "application.properties";

        Properties appProps = new Properties();
        try {
            appProps.load(new FileInputStream(appConfigPath));
        } catch (IOException e) {
            System.out.println("Got error while reading application.properties");
        }

        this.mailDir = appProps.getProperty("mail.dir");
        this.logPath = appProps.getProperty("logger.file-path");
        this.logLevel = LogLevel.valueOf(appProps.getProperty("logger.level"));
        this.socketTimeoutMs = Integer.valueOf(appProps.getProperty("socket.timeout.ms"));
    }

    public static ApplicationProperties instance() {
        return ApplicationPropertiesHolder.instance;
    }
}
