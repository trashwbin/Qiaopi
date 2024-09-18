package com.qiaopi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "UserRegisterDTO", description = "用户注册对象")
public class UserRegisterDTO extends UserLoginDTO{

    /**
     * 再次确认密码
     */
    @Schema(description = "再次确认密码" , required = true,example = "123456")
    private String confirmPassword;


}
