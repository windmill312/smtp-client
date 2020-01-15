package com.windmill312.smtp.client.common.logger;

import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LogQueue {
    private final ConcurrentLinkedQueue<String> queue;

    public static final class LogQueueHolder {
        static final LogQueue INSTANCE = new LogQueue();
    }

    private LogQueue() {
        this.queue = new ConcurrentLinkedQueue<>();
    }

    public static LogQueue instance() {
        return LogQueueHolder.INSTANCE;
    }

    synchronized void add(@Nonnull String item) {
        queue.add(item);
        notifyAll();
    }

    synchronized String poll() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        return queue.poll();
    }
}
