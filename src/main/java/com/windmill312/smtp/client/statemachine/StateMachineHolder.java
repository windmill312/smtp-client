package com.windmill312.smtp.client.statemachine;

import com.windmill312.smtp.client.statemachine.actions.ActionFactory;
import lombok.Getter;

import static com.windmill312.smtp.client.enums.Event.CONNECT;
import static com.windmill312.smtp.client.enums.Event.DATA;
import static com.windmill312.smtp.client.enums.Event.DATA_REQUEST;
import static com.windmill312.smtp.client.enums.Event.FINALIZE;
import static com.windmill312.smtp.client.enums.Event.HELO;
import static com.windmill312.smtp.client.enums.Event.MAIL_FROM;
import static com.windmill312.smtp.client.enums.Event.QUIT;
import static com.windmill312.smtp.client.enums.Event.RCPT_TO;
import static com.windmill312.smtp.client.enums.Mode.ANY;
import static com.windmill312.smtp.client.enums.Mode.READ;
import static com.windmill312.smtp.client.enums.Mode.WRITE;

public class StateMachineHolder {
    @Getter
    private final StateMachine stateMachine;

    private static final class StateMachineInstanceHolder {
        static final StateMachineHolder INSTANCE = new StateMachineHolder();
    }

    private StateMachineHolder() {
        final ActionFactory actionFactory = new ActionFactory();
        stateMachine = new StateMachineBuilder()
                .when(CONNECT, ANY).act(actionFactory.getAction(CONNECT, ANY))
                .when(CONNECT, READ).act(actionFactory.getAction(CONNECT, READ))

                .when(HELO, WRITE).act(actionFactory.getAction(HELO, WRITE))
                .when(HELO, READ).act(actionFactory.getAction(HELO, READ))

                .when(MAIL_FROM, WRITE).act(actionFactory.getAction(MAIL_FROM, WRITE))
                .when(MAIL_FROM, READ).act(actionFactory.getAction(MAIL_FROM, READ))

                .when(RCPT_TO, WRITE).act(actionFactory.getAction(RCPT_TO, WRITE))
                .when(RCPT_TO, READ).act(actionFactory.getAction(RCPT_TO, READ))

                .when(DATA_REQUEST, WRITE).act(actionFactory.getAction(DATA_REQUEST, WRITE))
                .when(DATA_REQUEST, READ).act(actionFactory.getAction(DATA_REQUEST, READ))

                .when(DATA, WRITE).act(actionFactory.getAction(DATA, WRITE))
                .when(DATA, READ).act(actionFactory.getAction(DATA, READ))

                .when(QUIT, WRITE).act(actionFactory.getAction(QUIT, WRITE))
                .when(QUIT, READ).act(actionFactory.getAction(QUIT, READ))

                .when(FINALIZE, ANY).act(actionFactory.getAction(FINALIZE, ANY))
                .build();
    }

    public static StateMachineHolder instance() {
        return StateMachineInstanceHolder.INSTANCE;
    }
}
