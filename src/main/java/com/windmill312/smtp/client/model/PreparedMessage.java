package com.windmill312.smtp.client.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class PreparedMessage {
    private final String from;
    private final String to;
    private final String data;
}
