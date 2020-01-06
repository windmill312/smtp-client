package com.windmill312.smtp.client.multiplexed.statemachine.actions;

import com.windmill312.smtp.client.common.logger.Logger;
import com.windmill312.smtp.client.multiplexed.statemachine.Action;
import com.windmill312.smtp.client.multiplexed.statemachine.StateMachineContext;
import com.windmill312.smtp.client.multiplexed.statemachine.StateMachineContextHolder;

import static com.windmill312.smtp.client.common.logger.LoggerFactory.getLogger;
import static com.windmill312.smtp.client.common.utils.SocketUtils.readFromChannel;
import static com.windmill312.smtp.client.multiplexed.enums.Event.DATA;
import static com.windmill312.smtp.client.multiplexed.enums.Event.FINALIZE;
import static com.windmill312.smtp.client.multiplexed.enums.Mode.ANY;

public class DataRequestReadAction
        implements Action {
    private static final Logger logger = getLogger(DataRequestReadAction.class);

    @Override
    public void execute(StateMachineContext context) {
        try {
            final StateMachineContextHolder contextHolder = context.getContextHolder();

            logger.debug("Execute DATA_REQUEST READ action for " + contextHolder.getMxRecord());

            int status = readFromChannel(contextHolder.getSelectionKey());

            if (status != 354) {
                logger.error("Failed to say DATA to " + contextHolder.getMxRecord() + ", status " + status);
                context.raise(FINALIZE, ANY);
                return;
            }

            contextHolder.setNextEvent(DATA);

        } catch (Exception e) {
            logger.error(e.getMessage());
            context.raise(FINALIZE, ANY);
        }
    }
}
