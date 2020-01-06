package com.windmill312.smtp.client.enums;

public enum Event {
    CONNECT,
    HELO,
    MAIL_FROM,
    RCPT_TO,
    DATA_REQUEST,
    DATA,
    QUIT,
    FINALIZE
}
