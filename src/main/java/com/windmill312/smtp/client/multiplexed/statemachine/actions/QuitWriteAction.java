package com.windmill312.smtp.client.multiplexed.statemachine.actions;

import com.windmill312.smtp.client.common.logger.Logger;
import com.windmill312.smtp.client.multiplexed.statemachine.Action;
import com.windmill312.smtp.client.multiplexed.statemachine.StateMachineContext;
import com.windmill312.smtp.client.multiplexed.statemachine.StateMachineContextHolder;

import static com.windmill312.smtp.client.common.logger.LoggerFactory.getLogger;
import static com.windmill312.smtp.client.common.utils.SocketUtils.writeToChannel;
import static com.windmill312.smtp.client.multiplexed.enums.Event.FINALIZE;
import static com.windmill312.smtp.client.multiplexed.enums.Mode.ANY;

public class QuitWriteAction implements Action {
    private static final Logger logger = getLogger(QuitWriteAction.class);

    @Override
    public void execute(StateMachineContext context) {
        try {
            final StateMachineContextHolder contextHolder = context.getContextHolder();

            logger.debug("Execute QUIT WRITE action for " + contextHolder.getMxRecord());

            writeToChannel(contextHolder.getSelectionKey(), "QUIT");

        } catch (Exception e) {
            logger.error(e.getMessage());
            context.raise(FINALIZE, ANY);
        }
    }
}
