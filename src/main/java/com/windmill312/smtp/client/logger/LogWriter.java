package com.windmill312.smtp.client.logger;

import com.windmill312.smtp.client.queue.LogQueue;

public class LogWriter implements Runnable, AutoCloseable {

    private volatile boolean stopped = false;

    private final LogQueue logQueue;

    public LogWriter() {
        this.logQueue = LogQueue.instance();
    }

    @Override
    public void close() {
        stopped = true;
    }

    @Override
    public void run() {
        System.out.println("LogWriter thread started");

        while (!stopped) {
            String logMessage = logQueue.dequeue();
            if (logMessage != null) {
                System.out.println(logMessage);
            }
        }

        System.out.println("LogWriter thread stopped");
    }
}
