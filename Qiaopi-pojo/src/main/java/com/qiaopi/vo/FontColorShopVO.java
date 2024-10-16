package com.qiaopi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "字体颜色")
public class FontColorShopVO {
    @Schema(description = "id")
    private Long id;

    @Schema(description = "颜色名称")
    private String description;

    @Schema(description="颜色的16进制代码")
    private String hexCode;

    @Schema(description="颜色的RGB代码")
    private String rgbValue;

    @Schema(description = "预览图片")
    private String previewImage;

    @Schema(description = "颜色价格")
    private int price;

    @Schema(description = "判断用户是否已拥有")
    private boolean isOwn;
}
