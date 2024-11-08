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



}
