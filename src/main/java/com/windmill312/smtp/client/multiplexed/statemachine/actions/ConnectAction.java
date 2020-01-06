package com.windmill312.smtp.client.multiplexed.statemachine.actions;

import com.windmill312.smtp.client.common.logger.Logger;
import com.windmill312.smtp.client.multiplexed.statemachine.Action;
import com.windmill312.smtp.client.multiplexed.statemachine.StateMachineContext;
import com.windmill312.smtp.client.multiplexed.statemachine.StateMachineContextHolder;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import static com.windmill312.smtp.client.common.logger.LoggerFactory.getLogger;
import static com.windmill312.smtp.client.multiplexed.enums.Event.FINALIZE;
import static com.windmill312.smtp.client.multiplexed.enums.Mode.ANY;

class ConnectAction
        implements Action {
    private static final Logger logger = getLogger(ConnectAction.class);

    private static final int DEFAULT_SOCKET_PORT = 25;

    @Override
    public void execute(StateMachineContext context) {
        try {
            final StateMachineContextHolder contextHolder = context.getContextHolder();

            logger.debug("Execute CONNECT action for " + contextHolder.getMxRecord());

            SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(false);

            SelectionKey selectionKey = channel.register(contextHolder.getSelector(), SelectionKey.OP_CONNECT);
            selectionKey.attach(context);
            contextHolder.setSelectionKey(selectionKey);

            logger.debug("Trying to connect to " + contextHolder.getMxRecord() + "...");
            channel.connect(new InetSocketAddress(contextHolder.getMxRecord(), DEFAULT_SOCKET_PORT));

        } catch (Exception e) {
            logger.error(e.getMessage());
            context.raise(FINALIZE, ANY);
        }
    }
}