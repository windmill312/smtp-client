package com.windmill312.smtp.client.multiplexed.statemachine;

import com.windmill312.smtp.client.common.model.DirectMessage;
import com.windmill312.smtp.client.multiplexed.enums.Step;
import lombok.Data;
import lombok.experimental.Accessors;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Queue;

@Data
@Accessors(chain = true)
public class StateMachineScopeHolder {
    private String domain;
    private String mxRecord;
    private Queue<DirectMessage> messages;
    private Step nextStep;
    private Selector selector;
    private SelectionKey selectionKey;
}
