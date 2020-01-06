package com.windmill312.smtp.client.statemachine.actions;

import com.windmill312.smtp.client.logger.Logger;
import com.windmill312.smtp.client.statemachine.Action;
import com.windmill312.smtp.client.statemachine.StateMachineContext;
import com.windmill312.smtp.client.statemachine.StateMachineContextHolder;

import static com.windmill312.smtp.client.enums.Event.FINALIZE;
import static com.windmill312.smtp.client.enums.Event.HELO;
import static com.windmill312.smtp.client.enums.Mode.ANY;
import static com.windmill312.smtp.client.logger.LoggerFactory.getLogger;
import static com.windmill312.smtp.client.utils.SocketUtils.readFromChannel;

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
