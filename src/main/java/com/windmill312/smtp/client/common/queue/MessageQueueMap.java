package com.windmill312.smtp.client.common.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageQueueMap<T> {

    private final Map<String, ConcurrentLinkedQueue<T>> messageQueueMap;

    public Set<String> getAllDomains() {
        return this.messageQueueMap.keySet();
    }

    public List<T> getAllForDomain(String domain) {
        return new ArrayList<>(messageQueueMap.remove(domain));
    }

    public static final class MessageQueueMapHolder {
        static final MessageQueueMap INSTANCE = new MessageQueueMap();
    }

    private MessageQueueMap() {
        this.messageQueueMap = new ConcurrentHashMap<>();
    }

    public static MessageQueueMap instance() {
        return MessageQueueMapHolder.INSTANCE;
    }

    public void putForDomain(String domain, T message) {
        if (this.messageQueueMap.containsKey(domain)) {
            this.messageQueueMap.get(domain).add(message);
        } else {
            ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<>();
            queue.add(message);
            this.messageQueueMap.put(domain, queue);
        }
    }

    public ConcurrentLinkedQueue<T> get(String key) {
        return this.messageQueueMap.get(key);
    }
}
