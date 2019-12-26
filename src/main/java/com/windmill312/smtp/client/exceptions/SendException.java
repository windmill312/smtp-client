package com.windmill312.smtp.client.exceptions;

public class SendException extends RuntimeException {
    public SendException() {
    }

    public SendException(String message) {
        super(message);
    }
}
