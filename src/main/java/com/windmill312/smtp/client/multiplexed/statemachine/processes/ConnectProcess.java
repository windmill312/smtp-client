package com.windmill312.smtp.client.multiplexed.statemachine.processes;

import com.windmill312.smtp.client.common.logger.Logger;
import com.windmill312.smtp.client.multiplexed.statemachine.Process;
import com.windmill312.smtp.client.multiplexed.statemachine.StateMachineScope;
import com.windmill312.smtp.client.multiplexed.statemachine.StateMachineScopeHolder;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import static com.windmill312.smtp.client.common.logger.LoggerFactory.getLogger;
import static com.windmill312.smtp.client.multiplexed.enums.Condition.UNDEFINED;
import static com.windmill312.smtp.client.multiplexed.enums.Step.FINALIZE;

class ConnectProcess
        implements Process {
    private static final Logger logger = getLogger(ConnectProcess.class);

    private static final int DEFAULT_SOCKET_PORT = 25;

    @Override
    public void execute(StateMachineScope context) {
        try {
            final StateMachineScopeHolder contextHolder = context.getContextHolder();

            logger.debug("Execute ATTACH action for " + contextHolder.getMxRecord());

            SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(false);

            SelectionKey selectionKey = channel.register(contextHolder.getSelector(), SelectionKey.OP_CONNECT);
            selectionKey.attach(context);
            contextHolder.setSelectionKey(selectionKey);

            logger.debug("Trying to connect to " + contextHolder.getMxRecord() + "...");
            channel.connect(new InetSocketAddress(contextHolder.getMxRecord(), DEFAULT_SOCKET_PORT));

        } catch (Exception e) {
            logger.error(e.getMessage());
            context.enhance(FINALIZE, UNDEFINED);
        }
    }
}
