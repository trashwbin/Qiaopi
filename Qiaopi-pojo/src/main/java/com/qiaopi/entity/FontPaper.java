package com.qiaopi.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "纸和字体所对应的适配字数")
public class FontPaper {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "纸张id")
    private Long paperId;
    @Schema(description = "字体id")
    private Long fontId;
    @Schema(description = "适配字数")
    private Long fitNumber;




}
