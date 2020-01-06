package com.windmill312.smtp.client.service;

import com.windmill312.smtp.client.config.ApplicationProperties;
import com.windmill312.smtp.client.logger.Logger;
import com.windmill312.smtp.client.queue.LogQueue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.windmill312.smtp.client.logger.LoggerFactory.getLogger;

public class LoggerService implements Runnable, AutoCloseable {
    private static final Logger logger = getLogger(LoggerService.class);
    private static final String DEFAULT_LOG_PATH = "/home/Desktop";
    private volatile boolean stopped = false;
    private final String logFileName = "client.log";
    private final ApplicationProperties properties;
    private final String dateTimeFormat = "yyyy-MM-dd HH:mm:ss.SSS";

    private final LogQueue logQueue;

    public LoggerService() {
        this.logQueue = LogQueue.instance();
        this.properties = ApplicationProperties.instance();
    }

    @Override
    public void close() {
        stopped = true;
    }

    @Override
    public void run() {
        logger.info("LoggerService thread started");
        try {
            while (!stopped) {
                String logMessage = logQueue.dequeue();
                if (logMessage != null) {
                    Files.write(
                            createOrGetLogFilePath(properties.getLogPath()),
                            (LocalDateTime.now().format(DateTimeFormatter.ofPattern(dateTimeFormat)) + " " + logMessage + "\n").getBytes(),
                            StandardOpenOption.APPEND
                    );
                }
            }
        } catch (IOException e) {
            logger.error("Got error while saving logs to file: " + e.getLocalizedMessage());
        }
        logger.info("LoggerService thread stopped");
    }

    private Path createOrGetLogFilePath(String logPath) {
        if (!Files.exists(Paths.get(logPath))) {
            try {
                Files.createDirectory(Paths.get(logPath));
            } catch (IOException e) {
                System.out.println("Can't create log directory, use default directory: " + DEFAULT_LOG_PATH);
                logPath = DEFAULT_LOG_PATH;
            }
        }

        Path filePath = Paths.get(logPath + File.separator + logFileName);
        if (!Files.exists(filePath)) {
            try {
                Files.createFile(filePath);
            } catch (IOException e) {
                System.out.println("Can't create log file: " + filePath.toString() + " Error: " + e.getLocalizedMessage());
                this.stopped = true;
            }
        }

        return filePath;
    }
}
