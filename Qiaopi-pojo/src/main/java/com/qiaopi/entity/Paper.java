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

    @Schema(description = "字体文件")
    private String FilePath;
}
