package com.qiaopi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaperVO {
    @Schema(description = "id")
    private Long id;

    @Schema(description = "纸张名称")
    private String name;

    @Schema(description = "预览图片")
    private String previewImage;
}
