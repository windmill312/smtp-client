package com.windmill312.smtp.client.multiplexed.statemachine;

import com.windmill312.smtp.client.multiplexed.statemachine.processes.ProcessFactory;
import lombok.Getter;

import static com.windmill312.smtp.client.multiplexed.enums.Condition.READ;
import static com.windmill312.smtp.client.multiplexed.enums.Condition.UNDEFINED;
import static com.windmill312.smtp.client.multiplexed.enums.Condition.WRITE;
import static com.windmill312.smtp.client.multiplexed.enums.Step.ATTACH;
import static com.windmill312.smtp.client.multiplexed.enums.Step.DATA;
import static com.windmill312.smtp.client.multiplexed.enums.Step.DATA_REQUEST;
import static com.windmill312.smtp.client.multiplexed.enums.Step.FINALIZE;
import static com.windmill312.smtp.client.multiplexed.enums.Step.HELO;
import static com.windmill312.smtp.client.multiplexed.enums.Step.MAIL_FROM;
import static com.windmill312.smtp.client.multiplexed.enums.Step.QUIT;
import static com.windmill312.smtp.client.multiplexed.enums.Step.RCPT_TO;

public class StateMachineHolder {
    @Getter
    private final StateMachine stateMachine;

    private static final class StateMachineInstanceHolder {
        static final StateMachineHolder INSTANCE = new StateMachineHolder();
    }

    private StateMachineHolder() {
        final ProcessFactory processFactory = new ProcessFactory();
        stateMachine = new StateMachineBuilder()
                .when(ATTACH, UNDEFINED).act(processFactory.getProcess(ATTACH, UNDEFINED))
                .when(ATTACH, READ).act(processFactory.getProcess(ATTACH, READ))

                .when(HELO, WRITE).act(processFactory.getProcess(HELO, WRITE))
                .when(HELO, READ).act(processFactory.getProcess(HELO, READ))

                .when(MAIL_FROM, WRITE).act(processFactory.getProcess(MAIL_FROM, WRITE))
                .when(MAIL_FROM, READ).act(processFactory.getProcess(MAIL_FROM, READ))

                .when(RCPT_TO, WRITE).act(processFactory.getProcess(RCPT_TO, WRITE))
                .when(RCPT_TO, READ).act(processFactory.getProcess(RCPT_TO, READ))

                .when(DATA_REQUEST, WRITE).act(processFactory.getProcess(DATA_REQUEST, WRITE))
                .when(DATA_REQUEST, READ).act(processFactory.getProcess(DATA_REQUEST, READ))

                .when(DATA, WRITE).act(processFactory.getProcess(DATA, WRITE))
                .when(DATA, READ).act(processFactory.getProcess(DATA, READ))

                .when(QUIT, WRITE).act(processFactory.getProcess(QUIT, WRITE))
                .when(QUIT, READ).act(processFactory.getProcess(QUIT, READ))

                .when(FINALIZE, UNDEFINED).act(processFactory.getProcess(FINALIZE, UNDEFINED))
                .build();
    }

    public static StateMachineHolder instance() {
        return StateMachineInstanceHolder.INSTANCE;
    }
}
