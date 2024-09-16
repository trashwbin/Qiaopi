package com.qiaopi.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "员工登录返回的数据格式")
public class UserLoginVO implements Serializable {

    @Schema(description = "主键值")
    private Long id;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "昵称")
    /** 用户昵称 */
    private String nickName;

    @Schema(description = "jwt令牌")
    private String token;

    /** 用户邮箱 */
    @Schema(description = "邮箱")
    private String email;

    /** 手机号码 */
//    private String phonenumber;

    /** 用户性别 */
    @Schema(description = "性别")
    private String sex;

    /** 用户头像 */
    @Schema(description = "头像")
    private String avatar;
}