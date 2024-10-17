package com.qiaopi.vo;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.qiaopi.BaseEntity;
import com.qiaopi.entity.Address;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Schema(description = "好友")
@AllArgsConstructor
@Data
@NoArgsConstructor
@TableName(autoResultMap = true)//自动映射
public class FriendRequestVO {
    private Long id;

    @Schema(description = "请求人id")
    private Long senderId;

    @Schema(description = "被请求人id")
    private Long receiverId;

    @Schema(description = "请求状态")
    private int status;

    /** 地址 */
    @Schema(description = "地址")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Address giveAddress;

    @Schema(description = "请求内容")
    private String content;

    @Schema(description = "请求人姓名")
    private String senderName;

    @Schema(description = "请求人头像")
    private String senderAvatar;

    @Schema(description = "请求发送时间")
    private String createTime;
}
