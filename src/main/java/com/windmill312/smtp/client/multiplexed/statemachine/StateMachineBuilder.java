package com.windmill312.smtp.client.multiplexed.statemachine;

import com.google.common.collect.ImmutableTable;
import com.windmill312.smtp.client.multiplexed.enums.Event;
import com.windmill312.smtp.client.multiplexed.enums.Mode;

public class StateMachineBuilder {

    private final ImmutableTable.Builder<Event, Mode, Action> builder;

    public StateMachineBuilder() {
        builder = ImmutableTable.builder();
    }

    public StateMachine build() {
        final StateMachine stateMachine = new StateMachine();
        return stateMachine.setTable(builder.build());
    }

    public ActionHolder when(Event event, Mode status) {
        return action -> {
            builder.put(event, status, action);
            return StateMachineBuilder.this;
        };
    }

    public interface ActionHolder {
        StateMachineBuilder act(Action action);
    }
}
