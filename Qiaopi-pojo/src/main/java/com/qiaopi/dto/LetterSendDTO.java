package com.qiaopi.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.qiaopi.entity.Address;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 信件发送对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "LetterSendDTO", description = "信件发送对象")
public class LetterSendDTO {


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
     * 信的链接(已弃用)
     */
//    @Schema(description = "信的链接")
//    private String letterLink;

    /**
     * 印章ID(后期预留)
     */
    @Schema(description = "印章ID")
    private Long signetId;

    /**
     * 携带猪仔钱
     */
    @Schema(description = "携带猪仔钱")
    private Long piggyMoney;

    /**
     * 信件类型(1:竖版字体信件,2:横版信件)
     */
    @Schema(description = "信件类型(1:竖版字体信件,2:横版信件)")
    private int letterType;
}
