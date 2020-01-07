package com.windmill312.smtp.client.multiplexed.statemachine.processes;

import com.windmill312.smtp.client.common.logger.Logger;
import com.windmill312.smtp.client.multiplexed.statemachine.Process;
import com.windmill312.smtp.client.multiplexed.statemachine.StateMachineScope;
import com.windmill312.smtp.client.multiplexed.statemachine.StateMachineScopeHolder;

import static com.windmill312.smtp.client.common.logger.LoggerFactory.getLogger;
import static com.windmill312.smtp.client.common.utils.SocketUtils.getResponseCodeFromChannel;
import static com.windmill312.smtp.client.multiplexed.enums.Condition.UNDEFINED;
import static com.windmill312.smtp.client.multiplexed.enums.Step.FINALIZE;
import static com.windmill312.smtp.client.multiplexed.enums.Step.MAIL_FROM;
import static com.windmill312.smtp.client.multiplexed.enums.Step.QUIT;

public class DataReadProcess
        implements Process {
    private static final Logger logger = getLogger(DataReadProcess.class);

    @Override
    public void execute(StateMachineScope context) {
        try {
            final StateMachineScopeHolder contextHolder = context.getContextHolder();

            logger.debug("Execute DATA READ action for " + contextHolder.getMxRecord());

            int status = getResponseCodeFromChannel(contextHolder.getSelectionKey());

            if (status != 250) {
                logger.error("Data content rejected for " + contextHolder.getMxRecord() + ", status " + status);
                context.enhance(FINALIZE, UNDEFINED);
                return;
            }

            contextHolder.getMessages().poll();

            if (contextHolder.getMessages().isEmpty()) {
                contextHolder.setNextStep(QUIT);
            } else {
                contextHolder.setNextStep(MAIL_FROM);
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            context.enhance(FINALIZE, UNDEFINED);
        }
    }
}
