package com.windmill312.smtp.client.service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class ThreadFactoryService {
    private final ExecutorService executorService;

    public ThreadFactoryService() {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("Service-%d")
                .setDaemon(true)
                .build();

        this.executorService = Executors.newFixedThreadPool(4, threadFactory);
    }

    public void start() {
        executorService.execute(new LoggerService());
        executorService.execute(new MessageReaderService());
        executorService.execute(new MessageQueueReaderService());
        executorService.execute(new MessageSenderService());
    }
}
