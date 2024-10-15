package com.qiaopi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.qiaopi.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@TableName("bottle")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Bottle extends BaseEntity {

    /**
     * 漂流瓶id
     */
/*
    @Schema(description = "漂流瓶id")
    private Long bottleId;
*/

    /**
     * 用户id
     */
    @Schema(description = "用户id")
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    @TableField(value = "nick_name")
    private String nickName;

    /**
     * 用户邮箱
     */
    @Schema(description = "用户邮箱")
    @TableField(value = "email")
    private String email;


    /**
     * 发送者地址
     */
    @Schema(description = "发送者地址")
    @TableField(value = "sender_address")
    private String senderAddress;


    /**
     * 漂流瓶内容
     */
    @Schema(description = "漂流瓶内容")
    @TableField(value = "content")
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

    /**
     * 创建时间
     */
    /*@Schema(description = "创建时间")
    @TableField(value = "created_time")
    private LocalDateTime createdTime;*/


    @Schema(description = "漂流瓶状态")
    @TableField(value = "is_picked")
    private Integer isPicked;

    @Schema(description = "漂流瓶图片地址")
    @TableField(value = "bottle_url")
    private String bottleUrl;


}
