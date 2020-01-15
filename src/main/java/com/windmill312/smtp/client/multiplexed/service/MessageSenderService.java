package com.windmill312.smtp.client.multiplexed.service;

import com.windmill312.smtp.client.common.logger.Logger;
import com.windmill312.smtp.client.multiplexed.ChannelsScope;
import com.windmill312.smtp.client.multiplexed.statemachine.StateMachineScope;

import javax.annotation.Nonnull;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import static com.windmill312.smtp.client.common.logger.LoggerFactory.getLogger;
import static com.windmill312.smtp.client.multiplexed.enums.Condition.READ;
import static com.windmill312.smtp.client.multiplexed.enums.Condition.WRITE;
import static com.windmill312.smtp.client.multiplexed.enums.Step.ATTACH;

public class MessageSenderService implements Runnable, AutoCloseable {

    private static final Logger logger = getLogger(MessageSenderService.class);
    private volatile boolean stopped = false;

    private final Selector selector;

    MessageSenderService() {
        this.selector = ChannelsScope.instance().getSelector();
    }

    @Override
    public void run() {
        try {
            logger.info("MessageSenderService thread started");

            while (!stopped) {
                selector.selectNow();

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();

                    if (key.isConnectable()) {
                        connect(key);
                    } else if (key.isReadable()) {
                        read(key);
                    } else if (key.isWritable()) {
                        write(key);
                    }

                    iterator.remove();
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            logger.info("MessageSenderService thread is stopped");
        }
    }

    @Override
    public void close() {
        stopped = true;
    }

    private void connect(@Nonnull SelectionKey key) {
        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            if (socketChannel.isConnectionPending()) {
                socketChannel.finishConnect();
            }

            if (socketChannel.isConnected()) {

                key.interestOps(key.interestOps() | SelectionKey.OP_READ);

                StateMachineScope context = (StateMachineScope) key.attachment();
                context.enhance(ATTACH, READ);
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Failed to perform connection. Reason: " + e.getLocalizedMessage());
            key.cancel();
        }
    }

    private void write(@Nonnull SelectionKey key) {
        StateMachineScope context = (StateMachineScope) key.attachment();
        context.enhance(context.getContextHolder().getNextStep(), WRITE);
    }

    private void read(@Nonnull SelectionKey key) {
        StateMachineScope context = (StateMachineScope) key.attachment();
        context.enhance(context.getContextHolder().getNextStep(), READ);
    }
}
