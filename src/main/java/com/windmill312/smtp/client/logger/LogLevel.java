package com.windmill312.smtp.client.logger;

import lombok.Getter;

public enum LogLevel {
    ERROR(0),
    WARN(1),
    INFO(2),
    DEBUG(3);

    @Getter
    private int order;

    LogLevel(int order) {
        this.order = order;
    }
}
