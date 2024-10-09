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

    @Schema(description = "id")
    private Long id;

    @Schema(description = "昵称")
    /** 用户昵称 */
    private String nickname;

    @Schema(description = "jwt令牌")
    private String token;

    /** 用户头像 */
    @Schema(description = "头像")
    private String avatar;

    /** 用户猪仔钱 */
//    @Schema(description = "猪仔钱")
//    private Long money;
}