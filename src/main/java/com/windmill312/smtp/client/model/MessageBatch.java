package com.windmill312.smtp.client.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class MessageBatch {
    private final List<DirectMessage> messages = new ArrayList<>();

    public void add(DirectMessage message) {
        messages.add(message);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (DirectMessage message : messages) {
            result.append("    ").append(message).append("\n");
        }
        return result.toString();
    }
}
