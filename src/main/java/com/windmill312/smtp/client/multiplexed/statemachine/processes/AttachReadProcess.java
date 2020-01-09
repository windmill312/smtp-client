package com.windmill312.smtp.client.multiplexed.statemachine.processes;

import com.windmill312.smtp.client.common.logger.Logger;
import com.windmill312.smtp.client.multiplexed.statemachine.Process;
import com.windmill312.smtp.client.multiplexed.statemachine.StateMachineScope;
import com.windmill312.smtp.client.multiplexed.statemachine.StateMachineScopeHolder;

import static com.windmill312.smtp.client.common.logger.LoggerFactory.getLogger;
import static com.windmill312.smtp.client.common.utils.SocketUtils.getResponseCodeFromChannel;
import static com.windmill312.smtp.client.multiplexed.enums.Condition.UNDEFINED;
import static com.windmill312.smtp.client.multiplexed.enums.Step.FINALIZE;
import static com.windmill312.smtp.client.multiplexed.enums.Step.HELO;

class AttachReadProcess implements Process {
    private static final Logger logger = getLogger(AttachReadProcess.class);

    @Override
    public void execute(StateMachineScope context) {
        try {
            final StateMachineScopeHolder contextHolder = context.getContextHolder();

            logger.debug("Execute ATTACHING READ action for " + contextHolder.getMxRecord());

            int status = getResponseCodeFromChannel(contextHolder.getSelectionKey());

            if (status != 220) {
                logger.error("Failed to attach to MX server " + contextHolder.getMxRecord() + ", status " + status);
                context.enhance(FINALIZE, UNDEFINED);
                return;
            }

            contextHolder.setNextStep(HELO);

        } catch (Exception e) {
            logger.error(e.getMessage());
            context.enhance(FINALIZE, UNDEFINED);
        }
    }
}
