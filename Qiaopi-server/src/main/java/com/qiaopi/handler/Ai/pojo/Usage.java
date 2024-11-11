package com.qiaopi.handler.Ai.pojo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Usage {
    private int prompt_tokens;
    private int completion_tokens;
    private int total_tokens;
}