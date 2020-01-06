package com.windmill312.smtp.client.common.logger;

import javax.annotation.Nonnull;

public interface QueueExchange<T> {
    void enqueue(@Nonnull T item);
    T dequeue();
}
