package com.windmill312.smtp.client.service;

import com.windmill312.smtp.client.ChannelsContext;
import com.windmill312.smtp.client.logger.Logger;
import com.windmill312.smtp.client.model.DirectMessage;
import com.windmill312.smtp.client.queue.MessageQueueMap;
import com.windmill312.smtp.client.statemachine.StateMachine;
import com.windmill312.smtp.client.statemachine.StateMachineContextHolder;
import com.windmill312.smtp.client.statemachine.StateMachineHolder;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

import static com.windmill312.smtp.client.enums.Event.CONNECT;
import static com.windmill312.smtp.client.enums.Mode.ANY;
import static com.windmill312.smtp.client.logger.LoggerFactory.getLogger;
import static com.windmill312.smtp.client.utils.MailUtils.getMxRecords;
import static java.lang.Thread.sleep;

public class MessageQueueReaderService
        implements Runnable, AutoCloseable {
    private static final Logger logger = getLogger(MessageQueueReaderService.class);

    private static final long DELAY_MILLIS = 2000L;
    private volatile boolean stopped = false;

    private final MessageQueueMap messageQueueMap;
    private final ChannelsContext channelsContext;
    private final StateMachine stateMachine;

    public MessageQueueReaderService() {
        this.messageQueueMap = MessageQueueMap.instance();
        this.channelsContext = ChannelsContext.instance();
        this.stateMachine = StateMachineHolder.instance().getStateMachine();
    }

    @Override
    public void run() {
        logger.info("MessageQueueReaderService thread started");
        try {
            while (!stopped) {
                messageQueueMap.getAllDomains()
                        .forEach(domain -> {
                            if (channelsContext.isChannelReady(domain)) {

                                final List<DirectMessage> messages = messageQueueMap.getAllForDomain(domain);

                                if (!messages.isEmpty()) {
                                    logger.debug("Trying to send " + messages.size() + " messages for domain: " + domain);

                                    channelsContext.setChannelNotReady(domain);
                                    sendMessages(domain, messages);
                                }
                            }
                        });

                sleep(DELAY_MILLIS);
            }
        } catch (InterruptedException exception) {
            logger.error("MessageQueueReaderService thread is interrupted");
        }
        logger.info("MessageQueueReaderService thread is stopped");
    }

    @Override
    public void close() {
        stopped = true;
    }

    private void sendMessages(@Nonnull String domain, @Nonnull List<DirectMessage> messages) {
        final List<String> mxRecords = getMxRecords(domain);
        if (mxRecords.isEmpty()) {
            logger.warn("No MX records found for domain " + domain);
            return;
        }

        final StateMachineContextHolder contextHolder = new StateMachineContextHolder()
                .setSelector(channelsContext.getSelector())
                .setNextEvent(CONNECT)
                .setDomain(domain)
                .setMxRecord(mxRecords.get(0))
                .setMessages(new LinkedList<>(messages));

        stateMachine.raise(CONNECT, ANY, contextHolder);
    }
}
