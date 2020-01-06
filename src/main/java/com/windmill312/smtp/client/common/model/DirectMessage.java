package com.windmill312.smtp.client.common.model;

import com.google.common.base.MoreObjects;
import com.windmill312.smtp.client.common.logger.Logger;
import com.windmill312.smtp.client.common.logger.LoggerFactory;
import com.windmill312.smtp.client.sequential.enums.MessageState;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DirectMessage {
    private final Logger logger = LoggerFactory.getLogger(DirectMessage.class);

    private String from;
    private String to;
    private String data;
    private MessageState state = MessageState.INIT;

    public void nextState() {
        setState(this.getState().nextState());
    }

    public void setState(MessageState state) {
        this.state = state;
        logger.debug("Message [" + this + "] has changed state to: " + this.getState());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("from", from)
                .add("to", to)
                .toString();
    }
}
