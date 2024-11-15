package com.qiaopi.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.qiaopi.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Schema(description = "好友")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Builder
@TableName(autoResultMap = true)//自动映射
public class FriendRequest extends BaseEntity {


    @Schema(description = "请求人id")
    private Long senderId;

    @Schema(description = "被请求人id")
    private Long receiverId;

    @Schema(description = "请求状态")
    private int status;

    /**
     * 地址
     */
    @Schema(description = "地址")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Address giveAddress;

    @Schema(description = "请求内容")
    private String content;

    @Schema(description = "漂流瓶id")
    private Long bottleId;

    @Schema(description = "信id")
    private Long letterId;
}
