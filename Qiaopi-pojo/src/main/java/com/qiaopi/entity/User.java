package com.qiaopi.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.qiaopi.BaseEntity;
import com.qiaopi.vo.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
@TableName(autoResultMap = true)//自动映射
public class User extends BaseEntity {

    private static final long serialVersionUID = 1L;


    /** 用户账号 */
    private String username;

    /** 用户昵称 */
    private String nickname;

    /** 用户邮箱 */
    private String email;

    /** 手机号码 */
//    private String phonenumber;

    /** 用户性别 */
    private String sex;

    /** 用户头像 */
    private String avatar;

    /** 密码 */
    private String password;

    /** 帐号状态（0正常 1停用） */
    private String status;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /** 最后登录IP */
    private String loginIp;

    /** 最后登录时间 */
    private LocalDateTime loginDate;

    /** 猪仔钱 */
    private Long money;

    /** 拥有纸张 */
    @TableField(typeHandler = JacksonTypeHandler.class)//自定义类型转换器，mybatis-plus默认不支持List类型
    private List<PaperVO> papers;

    /** 拥有字体 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<FontVO> fonts;

    /** 地址 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Address> addresses;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<SignetVO> signets;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<FontColorVO> fontColors;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<FunctionCardVO> functionCards;
}

