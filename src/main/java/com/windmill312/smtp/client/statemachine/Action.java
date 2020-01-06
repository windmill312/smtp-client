package com.windmill312.smtp.client.statemachine;

public interface Action {
    void execute(StateMachineContext context);
}
