package com.windmill312.smtp.client.enums;

public enum MessageState {
    INIT {
        @Override
        public MessageState nextState() {
            return HELO;
        }
    },

    HELO {
        @Override
        public MessageState nextState() {
            return MAIL_FROM;
        }
    },

    MAIL_FROM {
        @Override
        public MessageState nextState() {
            return RCPT_TO;
        }
    },

    RCPT_TO {
        @Override
        public MessageState nextState() {
            return DATA;
        }
    },

    DATA {
        @Override
        public MessageState nextState() {
            return SENT;
        }
    },

    SENT {
        @Override
        public MessageState nextState() {
            return SENT;
        }
    };

    public MessageState nextState() {
        return INIT;
    }
}
