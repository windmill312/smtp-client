package com.windmill312.smtp.client.service;

import com.windmill312.smtp.client.logger.Logger;
import com.windmill312.smtp.client.logger.LoggerFactory;
import com.windmill312.smtp.client.model.PreparedMessage;
import com.windmill312.smtp.client.queue.MessageQueueMap;

import java.util.concurrent.ConcurrentLinkedQueue;

import static com.windmill312.smtp.client.enums.Domain.MAIL_RU;
import static com.windmill312.smtp.client.service.SMTPMXLookUpService.isAddressValid;
import static java.lang.Thread.sleep;

public class MessageSenderService implements Runnable, AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(MessageSenderService.class);
    private static final Long DELAY_MILLIS = 1000L;
    private volatile boolean stopped = false;

    private final ConcurrentLinkedQueue<PreparedMessage> messageQueue;

    MessageSenderService() {
        this.messageQueue = MessageQueueMap.instance().get(MAIL_RU.value);
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
                PreparedMessage message = messageQueue.poll();
                if (message != null) {
                    if (isAddressValid(message)) {
                        System.out.println("Message: '" + message + "' successfully sent");
                    }
                }

                sleep(DELAY_MILLIS);
            }
        } catch (InterruptedException e) {
            System.out.println("MessageSender thread is interrupted");
        }

        System.out.println("MessageSender thread is stopped");
    }
}
