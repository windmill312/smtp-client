package com.windmill312.smtp.client.multiplexed.statemachine.actions;

import com.windmill312.smtp.client.common.logger.Logger;
import com.windmill312.smtp.client.multiplexed.statemachine.Action;
import com.windmill312.smtp.client.multiplexed.statemachine.StateMachineContext;
import com.windmill312.smtp.client.multiplexed.statemachine.StateMachineContextHolder;

import static com.windmill312.smtp.client.common.logger.LoggerFactory.getLogger;
import static com.windmill312.smtp.client.common.utils.SocketUtils.readFromChannel;
import static com.windmill312.smtp.client.multiplexed.enums.Event.FINALIZE;
import static com.windmill312.smtp.client.multiplexed.enums.Event.HELO;
import static com.windmill312.smtp.client.multiplexed.enums.Mode.ANY;

class ConnectReadAction
        implements Action {
    private static final Logger logger = getLogger(ConnectReadAction.class);

    @Override
    public void execute(StateMachineContext context) {
        try {
            final StateMachineContextHolder contextHolder = context.getContextHolder();

            logger.debug("Execute CONNECTION READ action for " + contextHolder.getMxRecord());

            int status = readFromChannel(contextHolder.getSelectionKey());

            if (status != 220) {
                logger.error("Failed to connect to MX server " + contextHolder.getMxRecord() + ", status " + status);
                context.raise(FINALIZE, ANY);
                return;
            }

            contextHolder.setNextEvent(HELO);

        } catch (Exception e) {
            logger.error(e.getMessage());
            context.raise(FINALIZE, ANY);
        }
    }
}
