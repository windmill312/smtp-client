package com.windmill312.smtp.client.multiplexed.statemachine.actions;

import com.windmill312.smtp.client.common.logger.Logger;
import com.windmill312.smtp.client.multiplexed.statemachine.Action;
import com.windmill312.smtp.client.multiplexed.statemachine.StateMachineContext;
import com.windmill312.smtp.client.multiplexed.statemachine.StateMachineContextHolder;

import static com.windmill312.smtp.client.common.logger.LoggerFactory.getLogger;
import static com.windmill312.smtp.client.common.utils.SocketUtils.readFromChannel;
import static com.windmill312.smtp.client.multiplexed.enums.Event.FINALIZE;
import static com.windmill312.smtp.client.multiplexed.enums.Event.RCPT_TO;
import static com.windmill312.smtp.client.multiplexed.enums.Mode.ANY;

public class MailFromReadAction
        implements Action {
    private static final Logger logger = getLogger(MailFromReadAction.class);

    @Override
    public void execute(StateMachineContext context) {
        try {
            final StateMachineContextHolder contextHolder = context.getContextHolder();

            logger.debug("Execute MAIL_FROM READ action for " + contextHolder.getMxRecord());

            int status = readFromChannel(contextHolder.getSelectionKey());

            if (status != 250) {
                logger.error("Failed to say MAIL FROM  to " + contextHolder.getMxRecord() + ", status " + status);
                context.raise(FINALIZE, ANY);
                return;
            }

            contextHolder.setNextEvent(RCPT_TO);

        } catch (Exception e) {
            logger.error(e.getMessage());
            context.raise(FINALIZE, ANY);
        }
    }
}
