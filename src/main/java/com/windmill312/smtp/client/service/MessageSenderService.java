package com.windmill312.smtp.client.service;

import com.windmill312.smtp.client.enums.Domain;
import com.windmill312.smtp.client.logger.Logger;
import com.windmill312.smtp.client.logger.LoggerFactory;
import com.windmill312.smtp.client.model.DirectMessage;
import com.windmill312.smtp.client.model.MessageBatch;
import com.windmill312.smtp.client.queue.MessageQueueMap;

import java.util.concurrent.ConcurrentLinkedQueue;

import static com.windmill312.smtp.client.enums.Domain.YANDEX_RU;
import static com.windmill312.smtp.client.service.SMTPMXLookUpService.sendBatch;
import static java.lang.Thread.sleep;

public class MessageSenderService implements Runnable, AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(MessageSenderService.class);
    private static final Long DELAY_MILLIS = 1000L;
    private static final int BATCH_SIZE = 5;
    private volatile boolean stopped = false;

    private final ConcurrentLinkedQueue<DirectMessage> directMessageQueue;

    MessageSenderService(Domain domain) {
        this.directMessageQueue = MessageQueueMap.instance().get(domain.value);
    }

    @Override
    public void close() {
        stopped = true;
    }

    @Override
    public void run() {
        System.out.println("MessageSender thread started");
        try {
            while(!stopped) {
                //todo add domain services
                MessageBatch messageBatch = new MessageBatch();
                for (int i = 0; i < BATCH_SIZE; i++) {
                    DirectMessage directMessage = directMessageQueue.poll();
                    if (directMessage != null) {
                        messageBatch.add(directMessage);
                    }
                }

                if (messageBatch.getMessages().size() != 0) {
                    if (sendBatch(messageBatch)) {
                        logger.info("Batch [\n" + messageBatch + "\n] successfully sent");
                    }
                }

                sleep(DELAY_MILLIS);
            }
        } catch (InterruptedException e) {
            System.out.println("MessageSender thread is interrupted: " + e.getLocalizedMessage());
        }

        System.out.println("MessageSender thread is stopped");
    }
}
