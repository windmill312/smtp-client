package com.windmill312.smtp.client.multiplexed.statemachine.processes;

import com.windmill312.smtp.client.common.logger.Logger;
import com.windmill312.smtp.client.multiplexed.statemachine.Process;
import com.windmill312.smtp.client.multiplexed.statemachine.StateMachineScope;
import com.windmill312.smtp.client.multiplexed.statemachine.StateMachineScopeHolder;

import static com.windmill312.smtp.client.common.logger.LoggerFactory.getLogger;
import static com.windmill312.smtp.client.common.utils.SocketUtils.writeToChannel;
import static com.windmill312.smtp.client.multiplexed.enums.Condition.UNDEFINED;
import static com.windmill312.smtp.client.multiplexed.enums.Step.FINALIZE;

public class MailFromWriteProcess implements Process {
    private static final Logger logger = getLogger(MailFromWriteProcess.class);

    @Override
    public void execute(StateMachineScope context) {
        try {
            final StateMachineScopeHolder contextHolder = context.getContextHolder();

            logger.debug("Execute MAIL FROM action for " + contextHolder.getMxRecord());

            writeToChannel(
                    contextHolder.getSelectionKey(),
                    "MAIL FROM: " + contextHolder.getMessages().peek().getFrom(),
                    false
            );

        } catch (Exception e) {
            logger.error(e.getMessage());
            context.enhance(FINALIZE, UNDEFINED);
        }
    }
}
