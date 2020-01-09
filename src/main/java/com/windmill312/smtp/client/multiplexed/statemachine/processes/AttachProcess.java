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

class AttachProcess implements Process {
    private static final Logger logger = getLogger(AttachProcess.class);

    private static final int DEFAULT_SOCKET_PORT = 25;

    @Override
    public void execute(StateMachineScope scope) {
        try {
            final StateMachineScopeHolder scopeHolder = scope.getContextHolder();

            logger.debug("Execute ATTACH action for " + scopeHolder.getMxRecord());

            SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(false);

            SelectionKey selectionKey = channel.register(scopeHolder.getSelector(), SelectionKey.OP_CONNECT);
            selectionKey.attach(scope);
            scopeHolder.setSelectionKey(selectionKey);

            logger.debug("Trying to connect to " + scopeHolder.getMxRecord() + "...");
            channel.connect(new InetSocketAddress(scopeHolder.getMxRecord(), DEFAULT_SOCKET_PORT));

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getLocalizedMessage());
            scope.enhance(FINALIZE, UNDEFINED);
        }
    }
}
