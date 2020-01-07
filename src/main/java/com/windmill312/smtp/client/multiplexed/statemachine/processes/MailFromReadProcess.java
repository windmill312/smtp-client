package com.windmill312.smtp.client.multiplexed.statemachine.processes;

import com.windmill312.smtp.client.common.logger.Logger;
import com.windmill312.smtp.client.multiplexed.statemachine.Process;
import com.windmill312.smtp.client.multiplexed.statemachine.StateMachineScope;
import com.windmill312.smtp.client.multiplexed.statemachine.StateMachineScopeHolder;

import static com.windmill312.smtp.client.common.logger.LoggerFactory.getLogger;
import static com.windmill312.smtp.client.common.utils.SocketUtils.getResponseCodeFromChannel;
import static com.windmill312.smtp.client.multiplexed.enums.Condition.UNDEFINED;
import static com.windmill312.smtp.client.multiplexed.enums.Step.FINALIZE;
import static com.windmill312.smtp.client.multiplexed.enums.Step.RCPT_TO;

public class MailFromReadProcess
        implements Process {
    private static final Logger logger = getLogger(MailFromReadProcess.class);

    @Override
    public void execute(StateMachineScope context) {
        try {
            final StateMachineScopeHolder contextHolder = context.getContextHolder();

            logger.debug("Execute MAIL_FROM READ action for " + contextHolder.getMxRecord());

            int status = getResponseCodeFromChannel(contextHolder.getSelectionKey());

            if (status != 250) {
                logger.error("Failed to say MAIL FROM  to " + contextHolder.getMxRecord() + ", status " + status);
                context.enhance(FINALIZE, UNDEFINED);
                return;
            }

            contextHolder.setNextStep(RCPT_TO);

        } catch (Exception e) {
            logger.error(e.getMessage());
            context.enhance(FINALIZE, UNDEFINED);
        }
    }
}
