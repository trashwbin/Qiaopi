package com.qiaopi.handler.Ai.pojo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class MyModelData {
    private String id;
    private long created;
    private String model;
    private List<Choice> choices;
    private Usage usage;
    private List<WebSearchResponse> web_search;
}