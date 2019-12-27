package com.windmill312.smtp.client.logger;

import com.windmill312.smtp.client.config.ApplicationProperties;
import com.windmill312.smtp.client.queue.LogQueue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class LogWriter implements Runnable, AutoCloseable {

    private static final Long DELAY_MILLIS = 1000L;
    private volatile boolean stopped = false;
    private final String logFileName = "client.log";
    private final ApplicationProperties properties;

    private final LogQueue logQueue;

    public LogWriter() {
        this.logQueue = LogQueue.instance();
        this.properties = ApplicationProperties.instance();
    }

    @Override
    public void close() {
        stopped = true;
    }

    @Override
    public void run() {
        System.out.println("LogWriter thread started");
        try {
            while (!stopped) {
                String logMessage = logQueue.dequeue();
                if (logMessage != null) {
                    Files.write(
                            createOrGetLogFilePath(properties.getLogPath()),
                            (logMessage + "\n").getBytes(),
                            StandardOpenOption.APPEND
                    );
                } else {
                    Thread.sleep(DELAY_MILLIS);
                }
            }
        } catch (InterruptedException e) {
            System.out.println("LogWriter thread is interrupted");
        } catch (IOException e) {
            System.out.println("Got error while saving logs to file: " + e.getLocalizedMessage());
        }
        System.out.println("LogWriter thread stopped");
    }

    private Path createOrGetLogFilePath(String logPath) {
        try {

            if (!Files.exists(Paths.get(logPath))) {
                Files.createDirectory(Paths.get(logPath));
            }

            Path filePath = Paths.get(logPath + File.separator + logFileName);
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }

            return filePath;

        } catch (IOException e) {
            System.out.println("Can't create log directory or file");
            return null;
        }
    }
}
