package com.qiaopi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.qiaopi.BaseEntity;

import java.time.LocalDateTime;

@TableName(autoResultMap = true)
public class Letter extends BaseEntity {
    /**
     * 寄件人的用户ID
     */
    private Long senderUserId;

    /**
     * 寄件人的姓名
     */
    private String senderName;

    /**
     * 收件人的邮箱
     */
    private String recipientEmail;

    /**
     * 收件人的用户ID(非必需项)
     */
    private Long recipientUserId;

    /**
     * 收件人的姓名
     */
    private String recipientName;

    /**
     * 信的内容
     */
    private String letterContent;

    /**
     * 信的链接
     */
    private String letterLink;

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
    private String status;

    /**
     * 送信进度
     */
    private String deliveryProgress;
}
