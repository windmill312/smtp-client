package com.windmill312.smtp.client.statemachine.actions;

import com.windmill312.smtp.client.enums.Mode;
import com.windmill312.smtp.client.logger.Logger;
import com.windmill312.smtp.client.statemachine.Action;
import com.windmill312.smtp.client.statemachine.StateMachineContext;
import com.windmill312.smtp.client.statemachine.StateMachineContextHolder;

import static com.windmill312.smtp.client.enums.Event.FINALIZE;
import static com.windmill312.smtp.client.logger.LoggerFactory.getLogger;
import static com.windmill312.smtp.client.utils.SocketUtils.writeToChannel;

class HeloWriteAction implements Action {
    private static final Logger logger = getLogger(HeloWriteAction.class);

    private static final String SERVER_NAME = "local.server";

    @Override
    public void execute(StateMachineContext context) {
        try {
            final StateMachineContextHolder contextHolder = context.getContextHolder();

            logger.debug("Execute HELO WRITE action for " + contextHolder.getMxRecord());

            writeToChannel(contextHolder.getSelectionKey(), "HELO " + SERVER_NAME);

        } catch (Exception e) {
            logger.error(e.getMessage());
            context.raise(FINALIZE, Mode.ANY);
        }
    }
}
