package com.windmill312.smtp.client.sequential.service;

import com.windmill312.smtp.client.common.logger.Logger;
import com.windmill312.smtp.client.common.logger.LoggerFactory;
import com.windmill312.smtp.client.common.model.DirectMessage;
import com.windmill312.smtp.client.common.queue.MessageQueueMap;
import com.windmill312.smtp.client.sequential.enums.Domain;
import com.windmill312.smtp.client.sequential.model.MessageBatch;

import java.util.concurrent.ConcurrentLinkedQueue;

import static com.windmill312.smtp.client.sequential.service.SmtpMxLookUpService.sendBatch;

public class MessageSenderService implements Runnable, AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(MessageSenderService.class);
    private static final int BATCH_SIZE = 5;
    private volatile boolean stopped = false;

    private Domain domain;

    MessageSenderService(Domain domain) {
        this.domain = domain;
    }

    @Override
    public void close() {
        stopped = true;
    }

    @Override
    public void run() {
        logger.info("MessageSender thread started");
        try {
            while(!stopped) {
                boolean hasDomainQueue = MessageQueueMap.instance().get(domain.value) != null;

                if (hasDomainQueue) {
                    ConcurrentLinkedQueue<DirectMessage> directMessageQueue = MessageQueueMap.instance().get(domain.value);
                    MessageBatch messageBatch = new MessageBatch();
                    for (int i = 0; i < BATCH_SIZE; i++) {
                        DirectMessage directMessage = directMessageQueue.poll();
                        if (directMessage != null) {
                            messageBatch.add(directMessage);
                        }
                    }

                    if (messageBatch.getMessages().size() != 0) {
                        if (sendBatch(messageBatch)) {
                            logger.debug("Batch [\n" + messageBatch + "\n] successfully sent");
                        }
                    }
                }
                wait();
            }
        } catch (InterruptedException e) {
            logger.warn("MessageSender thread is interrupted: " + e.getLocalizedMessage());
        }

        logger.info("MessageSender thread is stopped");
    }
}
