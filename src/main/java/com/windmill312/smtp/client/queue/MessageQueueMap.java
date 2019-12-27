package com.windmill312.smtp.client.queue;

import com.windmill312.smtp.client.model.DirectMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageQueueMap {

    private final Map<String, ConcurrentLinkedQueue<DirectMessage>> messageQueueMap;

    public static final class MessageQueueMapHolder {
        static final MessageQueueMap INSTANCE = new MessageQueueMap();
    }

    private MessageQueueMap() {
        this.messageQueueMap = new HashMap<>();
        this.messageQueueMap.put("yandex.ru", new ConcurrentLinkedQueue<>());
        this.messageQueueMap.put("google.com", new ConcurrentLinkedQueue<>());
        this.messageQueueMap.put("mail.ru", new ConcurrentLinkedQueue<>());
        this.messageQueueMap.put("default", new ConcurrentLinkedQueue<>());
    }

    public static MessageQueueMap instance() {
        return MessageQueueMapHolder.INSTANCE;
    }

    public ConcurrentLinkedQueue<DirectMessage> get(String key) {
        return this.messageQueueMap.get(key);
    }
}
