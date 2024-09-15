package com.qiaopi.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "员工登录返回的数据格式")
public class UserLoginVO implements Serializable {

    @ApiModelProperty("主键值")
    private Long id;

    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("昵称")
    /** 用户昵称 */
    private String nickName;

    @ApiModelProperty("jwt令牌")
    private String token;

    /** 用户邮箱 */
    @ApiModelProperty("邮箱")
    private String email;

    /** 手机号码 */
//    private String phonenumber;

    /** 用户性别 */
    @ApiModelProperty("性别")
    private String sex;

    /** 用户头像 */
    @ApiModelProperty("头像")
    private String avatar;
}