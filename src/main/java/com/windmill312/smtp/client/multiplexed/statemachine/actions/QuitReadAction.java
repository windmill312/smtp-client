package com.windmill312.smtp.client.multiplexed.statemachine.actions;

import com.windmill312.smtp.client.common.logger.Logger;
import com.windmill312.smtp.client.multiplexed.statemachine.Action;
import com.windmill312.smtp.client.multiplexed.statemachine.StateMachineContext;
import com.windmill312.smtp.client.multiplexed.statemachine.StateMachineContextHolder;

import static com.windmill312.smtp.client.common.logger.LoggerFactory.getLogger;
import static com.windmill312.smtp.client.common.utils.SocketUtils.readFromChannel;
import static com.windmill312.smtp.client.multiplexed.enums.Event.FINALIZE;
import static com.windmill312.smtp.client.multiplexed.enums.Mode.ANY;

public class QuitReadAction implements Action {
    private static final Logger logger = getLogger(QuitReadAction.class);

    @Override
    public void execute(StateMachineContext context) {
        try {
            final StateMachineContextHolder contextHolder = context.getContextHolder();

            logger.debug("Execute QUIT READ action for " + contextHolder.getMxRecord());

            int status = readFromChannel(contextHolder.getSelectionKey());

            if (status != 221) {
                logger.error("Failed to QUIT  from " + contextHolder.getMxRecord() + ", status " + status);
                context.raise(FINALIZE, ANY);
                return;
            }

            logger.info("SUCCESS TO SEND EMAILS to " + contextHolder.getMxRecord());

            context.raise(FINALIZE, ANY);

        } catch (Exception e) {
            logger.error(e.getMessage());
            context.raise(FINALIZE, ANY);
        }
    }
}
