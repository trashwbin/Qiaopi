package com.qiaopi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "BottlePaperDTO", description = "漂流瓶信纸生成对象")
public class BottlePaperDTO {

    /**
     * 漂流瓶内容
     */
    @Schema(description = "漂流瓶内容")
    private String content;


    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    private String nickName;



}
