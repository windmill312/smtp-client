package com.windmill312.smtp.client.multiplexed;

import lombok.Getter;
import lombok.SneakyThrows;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.channels.Selector;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class ChannelsScope {
    private final Selector selector;
    private final Map<String, Boolean> readyChannelsMap;

    private static final class ChannelsScopeHolder {
        static final ChannelsScope INSTANCE = new ChannelsScope();
    }

    @SneakyThrows(IOException.class)
    private ChannelsScope() {
        this.selector = Selector.open();
        this.readyChannelsMap = new ConcurrentHashMap<>();
    }

    public static ChannelsScope instance() {
        return ChannelsScopeHolder.INSTANCE;
    }

    public boolean isChannelReady(@Nonnull String domainName) {
        if (readyChannelsMap.get(domainName) == null) {
            return true;
        }
        return readyChannelsMap.get(domainName);
    }

    public void setChannelReady(@Nonnull String domainName) {
        readyChannelsMap.put(domainName, true);
    }

    public void setChannelNotReady(@Nonnull String domainName) {
        readyChannelsMap.put(domainName, false);
    }
}
