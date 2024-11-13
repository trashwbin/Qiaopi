package com.qiaopi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatDTO {
    private Long userId;
    private String message;
    private Boolean isWebSearch = true;
    private String chatModel = "GLM-4-Flash";

    public ChatDTO(Long userId, String content) {
        this.userId = userId;
        this.message = content;
    }
}
