package com.windmill312.smtp.client.multiplexed.statemachine;

public interface Action {
    void execute(StateMachineContext context);
}
