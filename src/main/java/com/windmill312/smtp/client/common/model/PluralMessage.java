package com.windmill312.smtp.client.common.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class PluralMessage {
    private final String from;
    private final List<String> to;
    private final String data;
}
