package com.windmill312.smtp.client.multiplexed.statemachine.processes;

import com.windmill312.smtp.client.common.logger.Logger;
import com.windmill312.smtp.client.multiplexed.ChannelsScope;
import com.windmill312.smtp.client.multiplexed.statemachine.Process;
import com.windmill312.smtp.client.multiplexed.statemachine.StateMachineScope;
import com.windmill312.smtp.client.multiplexed.statemachine.StateMachineScopeHolder;

import static com.windmill312.smtp.client.common.logger.LoggerFactory.getLogger;

public class FinalProcess implements Process {
    private static final Logger logger = getLogger(FinalProcess.class);

    @Override
    public void execute(StateMachineScope context) {
        try {
            final StateMachineScopeHolder contextHolder = context.getContextHolder();

            logger.debug("Execute FINALIZE action for " + contextHolder.getMxRecord());

            contextHolder.getSelectionKey().cancel();
            contextHolder.getSelectionKey().channel().close();

            ChannelsScope.instance().setChannelReady(contextHolder.getDomain());

        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
