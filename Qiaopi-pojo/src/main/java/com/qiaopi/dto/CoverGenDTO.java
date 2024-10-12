package com.qiaopi.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.qiaopi.entity.Address;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 封面生成对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "CoverGenDTO", description = "封面生成对象")
public class CoverGenDTO {


    /**
     * 寄件人的姓名
     */
    @Schema(description = "寄件人的姓名")
    private String senderName;

    /**
     * 收件人的用户ID(非必需项)
     */
    @Schema(description = "收件人的用户ID(非必需项)")
    private Long recipientUserId;

    /**
     * 收件人的姓名
     */
    @Schema(description = "收件人的姓名")
    private String recipientName;

    /**
     * 寄件人地址
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "寄件人地址")
    private Address senderAddress;

    /**
     * 收件人地址
     */
    @Schema(description = "收件人地址")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Address recipientAddress;

    /**
     * 印章ID
     */
    @Schema(description = "印章ID")
    private Long signetId;
}
