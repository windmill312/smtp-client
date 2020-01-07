package com.windmill312.smtp.client.multiplexed.service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.windmill312.smtp.client.common.logger.LoggerService;
import com.windmill312.smtp.client.common.service.MessageReaderService;
import com.windmill312.smtp.client.common.service.ThreadFactoryService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class ThreadFactoryServiceImpl implements ThreadFactoryService {
    private final ExecutorService executorService;

    public ThreadFactoryServiceImpl() {
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

    public void stop() {
        executorService.shutdownNow();
    }
}
