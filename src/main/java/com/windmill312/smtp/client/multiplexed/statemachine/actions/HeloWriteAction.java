package com.windmill312.smtp.client.multiplexed.statemachine.actions;

import com.windmill312.smtp.client.common.logger.Logger;
import com.windmill312.smtp.client.multiplexed.enums.Mode;
import com.windmill312.smtp.client.multiplexed.statemachine.Action;
import com.windmill312.smtp.client.multiplexed.statemachine.StateMachineContext;
import com.windmill312.smtp.client.multiplexed.statemachine.StateMachineContextHolder;

import static com.windmill312.smtp.client.common.logger.LoggerFactory.getLogger;
import static com.windmill312.smtp.client.common.utils.SocketUtils.writeToChannel;
import static com.windmill312.smtp.client.multiplexed.enums.Event.FINALIZE;

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
