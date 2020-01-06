package com.windmill312.smtp.client.multiplexed.statemachine;

import com.windmill312.smtp.client.multiplexed.enums.Event;
import com.windmill312.smtp.client.multiplexed.enums.Mode;

public interface StateMachineContext {
    void raise(Event event, Mode status);
    StateMachineContextHolder getContextHolder();
}
