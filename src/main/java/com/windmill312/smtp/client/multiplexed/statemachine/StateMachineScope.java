package com.windmill312.smtp.client.multiplexed.statemachine;

import com.windmill312.smtp.client.multiplexed.enums.Condition;
import com.windmill312.smtp.client.multiplexed.enums.Step;

public interface StateMachineScope {
    void enhance(Step step, Condition status);
    StateMachineScopeHolder getContextHolder();
}
