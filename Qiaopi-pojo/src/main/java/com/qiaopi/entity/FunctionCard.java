package com.qiaopi.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FunctionCard {
    @Schema(description = "主键")
    private Long id;
    @Schema(description = "卡片类型")
    private int cardType;
    @Schema(description = "卡片名称")
    private String cardName;
    @Schema(description = "卡片描述")
    private String cardDesc;
    @Schema(description = "卡片预览链接")
    private String cardPreviewLink;
    @Schema(description = "卡片状态")
    private int cardStatus;
    @Schema(description = "可减少时间")
    private String reduceTime;
    @Schema(description = "可加速速率")
    private String speedRate;
    @Schema(description = "备注")
    private String remark;
}
