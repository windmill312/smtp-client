package com.windmill312.smtp.client.statemachine;

import com.windmill312.smtp.client.enums.Event;
import com.windmill312.smtp.client.enums.Mode;

public interface StateMachineContext {
    void raise(Event event, Mode status);
    StateMachineContextHolder getContextHolder();
}
