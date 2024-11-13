package com.qiaopi.handler.Ai.pojo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Choice {
    private int index;
    private String finish_reason;
    private Delta delta;
}
