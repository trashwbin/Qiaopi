package com.qiaopi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString(callSuper = true)
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
