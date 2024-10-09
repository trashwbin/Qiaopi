package com.qiaopi.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "印章")
public class Signet {
    @Schema(description = "id")
    private Long id;

    @Schema(description = "字体名称")
    private String name;

    @Schema(description = "预览图片")
    private String previewImage;

    @Schema(description = "文件路径")
    private String FilePath;
}
