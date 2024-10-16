package com.qiaopi.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.qiaopi.entity.Address;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "BottleGenDTO", description = "漂流瓶生成对象")
public class BottleGenDTO {


    /**
     * 发送者地址
     */
    @Schema(description = "发送者地址")
    private Address senderAddress;


    /**
     * 漂流瓶内容
     */
    @Schema(description = "漂流瓶内容")
    private String content;

    /**
     * 字体类型
     */
    //@Schema(description = "字体类型")
    //private String fontId;

    /**
     * 字体颜色(以 HEX 格式存储)
     */
    //@Schema(description = "字体颜色")
    //private String fontColorId;

    /**
     * 纸张类型
     */
    //@Schema(description = "纸张类型")
    //private String paperId;



}
