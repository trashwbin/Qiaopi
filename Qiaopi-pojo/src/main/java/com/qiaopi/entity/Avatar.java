package com.qiaopi.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "头像")
public class Avatar {
    @Schema(description = "id")
    private Long id;
    @Schema(description = "头像名称")
    private String name;
    @Schema(description = "头像预览图片")
    private String url;
}
