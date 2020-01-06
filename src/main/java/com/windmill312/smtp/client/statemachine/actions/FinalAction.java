package com.windmill312.smtp.client.statemachine.actions;

import com.windmill312.smtp.client.ChannelsContext;
import com.windmill312.smtp.client.logger.Logger;
import com.windmill312.smtp.client.statemachine.Action;
import com.windmill312.smtp.client.statemachine.StateMachineContext;
import com.windmill312.smtp.client.statemachine.StateMachineContextHolder;

import static com.windmill312.smtp.client.logger.LoggerFactory.getLogger;

public class FinalAction implements Action {
    private static final Logger logger = getLogger(FinalAction.class);

    @Override
    public void execute(StateMachineContext context) {
        try {
            final StateMachineContextHolder contextHolder = context.getContextHolder();

            logger.debug("Execute FINALIZE action for " + contextHolder.getMxRecord());

            contextHolder.getSelectionKey().cancel();
            contextHolder.getSelectionKey().channel().close();

            ChannelsContext.instance().setChannelReady(contextHolder.getDomain());

        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
