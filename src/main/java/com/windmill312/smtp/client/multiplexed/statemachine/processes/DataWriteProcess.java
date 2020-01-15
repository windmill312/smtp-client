package com.windmill312.smtp.client.multiplexed.statemachine.processes;

import com.windmill312.smtp.client.common.config.ApplicationProperties;
import com.windmill312.smtp.client.common.logger.Logger;
import com.windmill312.smtp.client.common.model.DirectMessage;
import com.windmill312.smtp.client.multiplexed.statemachine.Process;
import com.windmill312.smtp.client.multiplexed.statemachine.StateMachineScope;
import com.windmill312.smtp.client.multiplexed.statemachine.StateMachineScopeHolder;

import java.util.Queue;

import static com.windmill312.smtp.client.common.logger.LoggerFactory.getLogger;
import static com.windmill312.smtp.client.common.utils.SocketUtils.writeToChannel;
import static com.windmill312.smtp.client.multiplexed.enums.Condition.UNDEFINED;
import static com.windmill312.smtp.client.multiplexed.enums.Step.DATA;
import static com.windmill312.smtp.client.multiplexed.enums.Step.FINALIZE;

public class DataWriteProcess implements Process {
    private static final Logger logger = getLogger(DataWriteProcess.class);
    private static final ApplicationProperties properties = ApplicationProperties.instance();

    @Override
    public void execute(StateMachineScope context) {
        try {
            final StateMachineScopeHolder contextHolder = context.getContextHolder();

            logger.debug("Execute DATA WRITE action for " + contextHolder.getMxRecord());
            DirectMessage message = contextHolder.getMessages().peek();
            String data = message.getData();
            int batchSize = properties.getBatchSize();

            if (message.getData().length() > batchSize) {
                writeToChannel(
                        contextHolder.getSelectionKey(),
                        data.substring(0, batchSize),
                        true
                );

                Queue<DirectMessage> messageQueue = contextHolder.getMessages();
                messageQueue.forEach(mes -> {
                    if (mes.hashCode() == message.hashCode()) {
                        mes.setData(message.getData().substring(batchSize));
                    }
                });

                contextHolder.setNextStep(DATA);
            } else {
                writeToChannel(
                        contextHolder.getSelectionKey(),
                        contextHolder.getMessages().peek().getData() + "\r\n.",
                        false
                );
            }


        } catch (Exception e) {
            logger.error(e.getMessage());
            context.enhance(FINALIZE, UNDEFINED);
        }
    }
}
