package com.windmill312.smtp.client.multiplexed.statemachine;

import com.google.common.collect.ImmutableTable;
import com.windmill312.smtp.client.multiplexed.enums.Condition;
import com.windmill312.smtp.client.multiplexed.enums.Step;

class StateMachineBuilder {

    private final ImmutableTable.Builder<Step, Condition, Process> builder;

    StateMachineBuilder() {
        builder = ImmutableTable.builder();
    }

    StateMachine build() {
        final StateMachine stateMachine = new StateMachine();
        return stateMachine.setTable(builder.build());
    }

    ActionHolder when(Step step, Condition status) {
        return action -> {
            builder.put(step, status, action);
            return StateMachineBuilder.this;
        };
    }

    public interface ActionHolder {
        StateMachineBuilder act(Process process);
    }
}
