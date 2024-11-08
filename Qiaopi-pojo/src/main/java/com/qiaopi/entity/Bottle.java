package com.qiaopi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.qiaopi.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper=false)
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(autoResultMap = true)
public class Bottle extends BaseEntity {

    /**
     * 漂流瓶id
     */
    @Schema(description = "漂流瓶id")
    private Long id;

    @Schema(description = "用户id")
    @TableField(value = "user_id")
    private Long userId;

    @Schema(description = "用户昵称")
    @TableField(value = "nick_name")
    private String nickName;

    @Schema(description = "用户邮箱")
    @TableField(value = "email")
    private String email;


    @Schema(description = "发送者地址")
    @TableField(value = "sender_address",typeHandler = JacksonTypeHandler.class)
    private Address senderAddress;

    @Schema(description = "漂流瓶内容")
    @TableField(value = "content")
    private String content;


    @Schema(description = "漂流瓶状态")
    @TableField(value = "is_picked")
    private boolean picked;

    @Schema(description = "漂流瓶图片地址")
    @TableField(value = "bottle_url")
    private String bottleUrl;
    // Getter 和 Setter 方法
    public boolean isPicked() {
        return picked;
    }

    public void setPicked(boolean picked) {
        this.picked = picked;
    }




}
