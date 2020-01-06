package com.windmill312.smtp.client.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DirectMessage {
    private String from;
    private String to;
    private String data;
}
