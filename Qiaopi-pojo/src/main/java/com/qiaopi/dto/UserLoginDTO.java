package com.qiaopi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录对象
 *
 * @author Abin
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "UserLoginDTO", description = "用户登录对象")
public class UserLoginDTO {
    /**
     * 用户名
     */
    //必填项
    @Schema(description = "用户名" , required = true,example = "admin")
    private String username;

    /**
     * 用户密码
     */
    @Schema(description = "用户密码" , required = true , example = "123456")
    private String password;

    /**
     * 验证码
     */
    @Schema(description = "验证码", required = true, example = "Xhe0p")
    private String code;

    /**
     * 唯一标识
     */
    @Schema(description = "唯一标识", example = "5df25fe5540f4f568add960e73b50f64")
    private String uuid;


}
