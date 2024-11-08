package com.qiaopi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.qiaopi.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Schema(description = "好友")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@TableName(autoResultMap = true)//自动映射
@Builder
public class Friend extends BaseEntity {

    @Schema(description = "好友id(非必需项)")
    private Long userId;
    @Schema(description = "所属用户id")
    private Long OwningId;
    @Schema(description = "好友名称")
    private String name;
    @Schema(description = "好友性别")
    private String sex;
    @Schema(description = "好友邮箱(必需项)")
    private String email;
    @Schema(description = "好友头像")
    private String avatar;
    /** 地址 */
    @Schema(description = "地址")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Address> addresses;
}
