package com.windmill312.smtp.client.multiplexed.statemachine.processes;

import com.windmill312.smtp.client.common.logger.Logger;
import com.windmill312.smtp.client.multiplexed.statemachine.Process;
import com.windmill312.smtp.client.multiplexed.statemachine.StateMachineScope;
import com.windmill312.smtp.client.multiplexed.statemachine.StateMachineScopeHolder;

import static com.windmill312.smtp.client.common.logger.LoggerFactory.getLogger;
import static com.windmill312.smtp.client.common.utils.SocketUtils.getResponseCodeFromChannel;
import static com.windmill312.smtp.client.multiplexed.enums.Condition.UNDEFINED;
import static com.windmill312.smtp.client.multiplexed.enums.Step.DATA;
import static com.windmill312.smtp.client.multiplexed.enums.Step.FINALIZE;

public class DataRequestReadProcess implements Process {
    private static final Logger logger = getLogger(DataRequestReadProcess.class);

    @Override
    public void execute(StateMachineScope context) {
        try {
            final StateMachineScopeHolder contextHolder = context.getContextHolder();

            logger.debug("Execute DATA_REQUEST READ action for " + contextHolder.getMxRecord());

            int status = getResponseCodeFromChannel(contextHolder.getSelectionKey());

            if (status != 354) {
                logger.error("Failed to say DATA to " + contextHolder.getMxRecord() + ", status " + status);
                context.enhance(FINALIZE, UNDEFINED);
                return;
            }

            contextHolder.setNextStep(DATA);

        } catch (Exception e) {
            logger.error(e.getMessage());
            context.enhance(FINALIZE, UNDEFINED);
        }
    }
}
