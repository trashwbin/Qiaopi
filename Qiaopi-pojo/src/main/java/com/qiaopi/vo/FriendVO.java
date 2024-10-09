package com.qiaopi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "好友VO")
@AllArgsConstructor
@Data
@NoArgsConstructor
public class FriendVO {
    @Schema(description = "序号id")
    private Long id;
    @Schema(description = "好友id(非必需项)")
    private Long userId;
    @Schema(description = "好友名称")
    private String name;
    @Schema(description = "好友性别")
    private String sex;
    @Schema(description = "好友邮箱(必需项)")
    private String email;
    /** 地址 */
//    @TableField(typeHandler = JacksonTypeHandler.class)
//    @Schema(description = "地址")
//    private List<Address> addresses;
}
