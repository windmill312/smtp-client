package com.windmill312.smtp.client.sequential.service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.windmill312.smtp.client.common.logger.LoggerService;
import com.windmill312.smtp.client.common.service.MessageReaderService;
import com.windmill312.smtp.client.common.service.ThreadFactoryService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static com.windmill312.smtp.client.sequential.enums.Domain.GOOGLE_COM;
import static com.windmill312.smtp.client.sequential.enums.Domain.MAIL_RU;
import static com.windmill312.smtp.client.sequential.enums.Domain.YANDEX_RU;

public class ThreadFactoryServiceImpl implements ThreadFactoryService {
    private final ExecutorService executorService;

    public ThreadFactoryServiceImpl() {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("Service-%d")
                .setDaemon(true)
                .build();

        this.executorService = Executors.newFixedThreadPool(5, threadFactory);
    }

    public void start() {
        executorService.execute(new LoggerService());
        executorService.execute(new MessageReaderService());
        executorService.execute(new MessageSenderService(YANDEX_RU));
        executorService.execute(new MessageSenderService(GOOGLE_COM));
        executorService.execute(new MessageSenderService(MAIL_RU));
    }

    public void stop() {
        executorService.shutdownNow();
    }
}
