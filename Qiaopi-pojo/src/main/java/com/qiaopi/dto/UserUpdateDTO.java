package com.qiaopi.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息更新对象
 *
 * @author Abin
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "UserUpdateDTO", description = "用户信息更新对象")
public class UserUpdateDTO {

    /**
     * 用户名
     */
    @Schema(description = "用户名" ,example = "admin")
    private String username;

    /**
     * 用户原密码
     */
    @Schema(description = "用户原密码" , example = "123456")
    private String oldPassword;

    /**
     * 用户新密码
     */
    @Schema(description = "用户新密码" , example = "12345678")
    private String newPassword;

    /**
     * 用户确认密码
     */
    @Schema(description = "用户确认密码" , example = "12345678")
    private String confirmPassword;

    /*用户邮箱(暂不支持换绑)*/
    //private String email;

    /**用户昵称*/
    @Schema(description = "用户昵称" , example = "侨批")
    private String nickname;

    /** 用户头像 */
    @Schema(description = "用户头像id" , example = "1")
    private Long avatarId;

    /** 用户性别 */
    @Schema(description = "用户性别" , example = "男")
    private String sex;
}
