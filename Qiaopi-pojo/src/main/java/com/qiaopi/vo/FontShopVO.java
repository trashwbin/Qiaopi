package com.qiaopi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FontShopVO {
    @Schema(description = "id")
    private Long id;

    @Schema(description = "字体名称")
    private String name;

    @Schema(description = "预览图片")
    private String previewImage;

    @Schema(description = "字体价格")
    private int price;
    @Schema(description = "判断用户是否已拥有")
    private boolean isOwn;
}
