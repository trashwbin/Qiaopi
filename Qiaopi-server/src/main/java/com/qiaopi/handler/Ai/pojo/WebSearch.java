//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.qiaopi.handler.Ai.pojo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;

public class WebSearch extends ObjectNode {
    private Boolean enable;
    private Boolean search_result;
    private String search_query;
    private String search_prompt;

    public WebSearch() {
        super(JsonNodeFactory.instance);
    }

    public WebSearch(JsonNodeFactory nc, Map<String, JsonNode> kids) {
        super(nc, kids);
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
        this.put("enable", enable);
    }

    public void setSearch_result(Boolean search_result) {
        this.search_result = search_result;
        this.put("search_result", search_result);
    }

    public void setSearch_query(String search_query) {
        this.search_query = search_query;
        this.put("search_query", search_query);
    }
    public void setSearch_prompt(String search_prompt) {
        this.search_prompt = search_prompt;
        this.put("search_prompt", search_prompt);
    }

    public String getSearch_prompt() {
        return this.search_prompt;
    }

    public Boolean getEnable() {
        return this.enable;
    }

    public Boolean getSearch_result() {
        return this.search_result;
    }

    public String getSearch_query() {
        return this.search_query;
    }
}
