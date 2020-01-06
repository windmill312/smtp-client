package com.windmill312.smtp.client.statemachine;

import com.windmill312.smtp.client.enums.Event;
import com.windmill312.smtp.client.model.DirectMessage;
import lombok.Data;
import lombok.experimental.Accessors;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Queue;

@Data
@Accessors(chain = true)
public class StateMachineContextHolder {
    private String domain;
    private String mxRecord;
    private Queue<DirectMessage> messages;
    private Event nextEvent;
    private Selector selector;
    private SelectionKey selectionKey;
}
