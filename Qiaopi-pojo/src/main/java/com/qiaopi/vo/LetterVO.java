package com.qiaopi.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.qiaopi.entity.Address;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LetterVO {
    /**
     * 寄件人的姓名
     */
    private String senderName;

    /**
     * 收件人的邮箱
     */
    private String recipientEmail;

    /**
     * 收件人的姓名
     */
    private String recipientName;

    /**
     * 信的链接
     */
    private String letterLink;

    /**
     * 封面链接
     */
    private String coverLink;

    /**
     * 寄件人地址
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Address senderAddress;

    /**
     * 收件人地址
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Address recipientAddress;

    /**
     * 预计送达时间
     */
    private LocalDateTime expectedDeliveryTime;

    /**
     * 信件状态(0:未发送 1:已发送,2:传递中,3:已送达)
     */
    private int status;

    /**
     * 送信进度(0-10000)
     */
    private Long deliveryProgress;

    /**
     * 阅读状态(0:未读,1:已读)
     */
    private int readStatus;
}
