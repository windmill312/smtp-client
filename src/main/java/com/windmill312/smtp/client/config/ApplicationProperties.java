package com.windmill312.smtp.client.config;

import lombok.Getter;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public final class ApplicationProperties {

    @Getter
    private final String mailDir;

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
    }

    public static ApplicationProperties instance() {
        return ApplicationPropertiesHolder.instance;
    }
}
