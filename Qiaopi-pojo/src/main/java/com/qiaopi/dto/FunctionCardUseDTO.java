package com.qiaopi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(name = "FunctionCardUseDTO", description = "功能卡使用DTO")
@Data
public class FunctionCardUseDTO {
    @Schema(description = "书信ID")
    private Long letterId;
    @Schema(description = "功能卡ID")
    private Long cardId;
}
