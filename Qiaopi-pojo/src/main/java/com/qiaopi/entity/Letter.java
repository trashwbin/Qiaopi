package com.qiaopi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.qiaopi.BaseEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@TableName(autoResultMap = true)
//改为getter和setter才能通过baseEntity的属性排序
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    /**
     * 寄信人的邮箱
     */
    private String senderEmail;

    /**
     * 携带猪仔钱
     */
    private Long piggyMoney;

    /**
     * 信件类型(1:竖版字体信件,2:横版信件)
     */
    private int letterType;

    /**
     * 加速倍率
     */
    private String speedRate;

    /**
     * 减少的时间(单位:分钟)
     */
    private String reduceTime;

    /**
     * 送达时间(用于定时任务送达)
     */
    private LocalDateTime deliveryTime;
}
