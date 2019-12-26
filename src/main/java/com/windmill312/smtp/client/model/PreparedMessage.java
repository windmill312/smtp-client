package com.windmill312.smtp.client.model;

import com.google.common.base.MoreObjects;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class PreparedMessage {
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
