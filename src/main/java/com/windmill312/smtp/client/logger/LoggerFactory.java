package com.windmill312.smtp.client.logger;

import javax.annotation.Nonnull;

public class LoggerFactory {

    @Nonnull
    public static Logger getLogger(@Nonnull Class clazz) {
        return new LoggerImpl(clazz);
    }
}
