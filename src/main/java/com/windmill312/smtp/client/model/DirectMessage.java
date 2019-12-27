package com.windmill312.smtp.client.model;

import com.google.common.base.MoreObjects;
import com.windmill312.smtp.client.enums.MessageState;
import com.windmill312.smtp.client.logger.Logger;
import com.windmill312.smtp.client.logger.LoggerFactory;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class DirectMessage {

    private Logger logger = LoggerFactory.getLogger(DirectMessage.class);
    private MessageState state = MessageState.INIT;
    private final MessageBody body;

    public void nextState() {
        setState(this.getState().nextState());
    }

    public void setState(MessageState state) {
        this.state = state;
        logger.debug("Message [" + this + "] has changed state to: " + this.getState());
    }

    @Data
    @RequiredArgsConstructor
    public static class MessageBody {
        private final String from;
        private final String to;
        private final String data;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("from", from)
                    .add("to", to)
                    .add("data", data)
                    .toString();
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("state", state)
                .add("body", body)
                .toString();
    }
}
