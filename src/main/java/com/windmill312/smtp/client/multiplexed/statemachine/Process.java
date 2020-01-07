package com.windmill312.smtp.client.multiplexed.statemachine;

public interface Process {
    void execute(StateMachineScope context);
}
