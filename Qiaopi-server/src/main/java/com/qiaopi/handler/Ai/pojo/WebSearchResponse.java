package com.qiaopi.handler.Ai.pojo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class WebSearchResponse {
    private String refer;
    private String title;
    private String content;
    private String link;
    private String media;
    private String icon;
}