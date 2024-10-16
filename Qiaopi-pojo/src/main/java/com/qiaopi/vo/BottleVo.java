package com.qiaopi.vo;


import com.baomidou.mybatisplus.annotation.TableField;
import com.qiaopi.entity.Address;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BottleVo {

    /**
     * 漂流瓶id
     */
    @Schema(description = "漂流瓶id")
    @TableField(value = "id")
    private Long id;


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
    private Address senderAddress;


    /**
     * 漂流瓶内容
     */
    @Schema(description = "漂流瓶内容")
    @TableField(value = "content")
    private String content;

    /**
     * 漂流瓶图片地址
     */
    @Schema(description = "漂流瓶图片地址")
    @TableField(value = "bottle_url")
    private String bottleUrl;


}
