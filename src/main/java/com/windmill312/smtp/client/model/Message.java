package com.windmill312.smtp.client.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class Message {
    private final String from;
    private final List<String> to;
    private final String data;
}
