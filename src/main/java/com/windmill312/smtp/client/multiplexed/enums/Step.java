package com.windmill312.smtp.client.multiplexed.enums;

public enum Step {
    ATTACH,
    HELO,
    MAIL_FROM,
    RCPT_TO,
    DATA_REQUEST,
    DATA,
    QUIT,
    FINALIZE
}
