package com.qiaopi.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "纸张")
public class Paper {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "纸张名称")
    private String name;

    @Schema(description = "预览图片")
    private String previewImage;

    @Schema(description = "纸张路径")
    private String FilePath;

    @Schema(description = "字体大小")
    private String fontSize;

    @Schema(description = "x偏移量")
    private String translateX;

    @Schema(description = "y偏移量")
    private String translateY;
}
