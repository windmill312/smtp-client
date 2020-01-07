package com.windmill312.smtp.client.multiplexed.statemachine.processes;

import com.windmill312.smtp.client.common.logger.Logger;
import com.windmill312.smtp.client.multiplexed.enums.Condition;
import com.windmill312.smtp.client.multiplexed.statemachine.Process;
import com.windmill312.smtp.client.multiplexed.statemachine.StateMachineScope;
import com.windmill312.smtp.client.multiplexed.statemachine.StateMachineScopeHolder;

import static com.windmill312.smtp.client.common.logger.LoggerFactory.getLogger;
import static com.windmill312.smtp.client.common.utils.SocketUtils.writeToChannel;
import static com.windmill312.smtp.client.multiplexed.enums.Step.FINALIZE;

class HeloWriteProcess implements Process {
    private static final Logger logger = getLogger(HeloWriteProcess.class);

    private static final String SERVER_NAME = "local.server";

    @Override
    public void execute(StateMachineScope context) {
        try {
            final StateMachineScopeHolder contextHolder = context.getContextHolder();

            logger.debug("Execute HELO WRITE action for " + contextHolder.getMxRecord());

            writeToChannel(contextHolder.getSelectionKey(), "HELO " + SERVER_NAME);

        } catch (Exception e) {
            logger.error(e.getMessage());
            context.enhance(FINALIZE, Condition.UNDEFINED);
        }
    }
}
