package com.qiaopi.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.qiaopi.entity.Address;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 信件生成对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "LetterGenDTO", description = "信件生成对象")
public class LetterGenDTO {


    /**
     * 寄件人的姓名
     */
    @Schema(description = "寄件人的姓名")
    private String senderName;

    /**
     * 收件人的邮箱
     */
    @Schema(description = "收件人的邮箱")
    private String recipientEmail;

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
     * 信的内容
     */
    @Schema(description = "信的内容")
    private String letterContent;

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
     * 字体ID
     */
    @Schema(description = "字体ID")
    private Long fontId;

    /**
     * 纸张ID
     */
    @Schema(description = "纸张ID")
    private Long paperId;

    /**
     * 字体颜色ID
     */
    @Schema(description = "字体颜色ID")
    private Long fontColorId;

}
