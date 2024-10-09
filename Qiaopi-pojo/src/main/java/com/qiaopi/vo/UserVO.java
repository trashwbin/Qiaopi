package com.qiaopi.vo;

import com.qiaopi.entity.Font;
import com.qiaopi.entity.Paper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class UserVO {

    private Long id;

    private String username;

    private String nickname;
    /** 用户头像 */
    private String avatar;
    /** 用户性别 */
    private String sex;

    private String email;
//    /**
//     * 猪仔钱
//     */
//    private Long money;
//    /**
//     * 拥有纸张
//     */
//    private List<PaperVO> papers;
//
//    /**
//     * 拥有字体
//     */
//    private List<FontVO> fonts;
}
