package com.qiaopi.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import com.qiaopi.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Schema(description = "好友")
@AllArgsConstructor
@Data
@NoArgsConstructor
@TableName(autoResultMap = true)//自动映射
public class FriendRequest extends BaseEntity {


    @Schema(description = "请求人id")
    private Long senderId;

    @Schema(description = "被请求人id")
    private Long receiverId;

    @Schema(description = "请求状态")
    private int status;









}
